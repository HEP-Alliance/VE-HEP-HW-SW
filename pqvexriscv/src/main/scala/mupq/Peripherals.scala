package mupq

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.com.eth._
import spinal.lib.com.uart._
import spinal.lib.misc.Clint
import spinal.lib.io._
import spinal.lib.com.spi.ddr._

trait Peripheral[T <: PQVexRiscv] extends Nameable {
  setName(this.getClass.getSimpleName.replace("$",""))

  def setup(soc: T): Unit = {}

  def build(soc: T): Unit

  implicit class implicitsSoC(soc: PQVexRiscv){
    def plug[T <: Area](area : T) = {area.setName(getName()).reflectNames();area}
  }
}

class GPIOPeripheral(val gpioWidth: Int, val addrOffset: BigInt = 0x0000) extends Peripheral[PQVexRiscv] {

  var gpio : TriStateArray = null

  override def setup(soc: PQVexRiscv) = {
    gpio = master(TriStateArray(gpioWidth bit)) setName("gpio")
  }

  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {
      val gpioCtrl = Apb3Gpio(gpioWidth = gpioWidth, withReadSync = true)
      gpio <> gpioCtrl.io.gpio
      soc.apbSlaves += gpioCtrl.io.apb -> (addrOffset, 64 KiB)
    }
  }
}

class UARTPeripheral(val addrOffset: BigInt = 0x10000) extends Peripheral[PQVexRiscv] {

  var uart : Uart = null

  override def setup(soc: PQVexRiscv) = {
    uart = master(Uart()) setName("uart")
  }

  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {
      val uartCtrlConfig = UartCtrlMemoryMappedConfig(
        uartCtrlConfig = UartCtrlGenerics(
          dataWidthMax = 8,
          clockDividerWidth = 20,
          preSamplingSize = 1,
          samplingSize = 3,
          postSamplingSize = 1
        ),
        initConfig = UartCtrlInitConfig(
          baudrate = 115200,
          dataLength = 7, //7 => 8 bits
          parity = UartParityType.NONE,
          stop = UartStopType.ONE
        ),
        busCanWriteClockDividerConfig = false,
        busCanWriteFrameConfig = false,
        txFifoDepth = 16,
        rxFifoDepth = 16
      )
      val uartCtrl = Apb3UartCtrl(uartCtrlConfig)
      uart <> uartCtrl.io.uart
      soc.core.externalInterrupt setWhen (uartCtrl.io.interrupt)
      soc.apbSlaves += uartCtrl.io.apb -> (addrOffset, 64 KiB)
    }
  }
}

class MDIOPeripheral(val addrOffset: BigInt = 0x20000) extends Peripheral[PQVexRiscv] {
  var mdio : Mdio = null

  val params = SpiXdrMasterCtrl.MemoryMappingParameters(
    ctrl = SpiXdrMasterCtrl.Parameters(
      dataWidth = 8,
      timerWidth = 12,
      spi = SpiXdrParameter(
        dataWidth = 1,
        ioRate = 1,
        ssWidth = 1
      )
    ).addHalfDuplex(0, 1, false, 1)
  )

  override def setup(soc: PQVexRiscv) = {
    mdio = master(Mdio()) setName("mdio")
  }

  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {
      val spiCtrl = new Apb3SpiXdrMasterCtrl(params)
      mdio <> spiCtrl.io.spi.toMdio()
      soc.core.externalInterrupt setWhen (spiCtrl.io.interrupt)
      soc.apbSlaves += spiCtrl.io.apb -> (addrOffset, 64 KiB)
    }
  }
}

object Apb3Clint{
  def getApb3Config = Apb3Config(
    addressWidth = 16,
    dataWidth = 32,
    selWidth = 1,
    useSlaveError = false
  )
}

case class Apb3Clint(hartCount : Int) extends Component{
  val io = new Bundle {
    val bus = slave(Apb3(Apb3Clint.getApb3Config))
    val timerInterrupt = out Bits(hartCount bits)
    val softwareInterrupt = out Bits(hartCount bits)
    val time = out UInt(64 bits)
  }

  val factory = Apb3SlaveFactory(io.bus)
  val logic = Clint(hartCount)
  logic.driveFrom(factory)

  for(hartId <- 0 until hartCount){
    io.timerInterrupt(hartId) := logic.harts(hartId).timerInterrupt
    io.softwareInterrupt(hartId) := logic.harts(hartId).softwareInterrupt
  }

  io.time := logic.time
}


class TimerPeripheral(val addrOffset: BigInt = 0x30000) extends Peripheral[PQVexRiscv] {
  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {
      val timer = Apb3Clint(1)
      soc.core.timerInterrupt setWhen (timer.io.timerInterrupt(0))
      soc.core.softwareInterrupt setWhen (timer.io.softwareInterrupt(0))
      soc.apbSlaves += timer.io.bus -> (addrOffset, 64 KiB)
    }
  }
}

object Apb3MacEth {
  def apb3Config = Apb3Config(
    addressWidth = 6,
    dataWidth = 32,
    selWidth = 1,
    useSlaveError = false
  )
}

case class Apb3MacEth(
  p : MacEthParameter,
  txCd : ClockDomain,
  rxCd : ClockDomain
) extends Component{
  val io = new Bundle{
    val bus =  slave(Apb3(Apb3MacEth.apb3Config))
    val phy = master(PhyIo(p.phy))
    val interrupt = out Bool()
  }

  val mac = new MacEth(p, txCd, rxCd)
  io.phy <> mac.io.phy

  val busCtrl = Apb3SlaveFactory(io.bus)
  val bridge = mac.io.ctrl.driveFrom(busCtrl)
  io.interrupt := bridge.interruptCtrl.pending
}

class MacPeripheral(val addrOffset: BigInt = 0x40000) extends Peripheral[PQVexRiscv] {

  var mii : Mii = null

  override def setup(soc: PQVexRiscv) = {
    val miiParam = MiiParameter(
      MiiTxParameter(4, false),
      MiiRxParameter(4)
    )
    mii = master(Mii(miiParam)) setName("mii")
  }

  override def build(soc: PQVexRiscv) = {
    val txCd = ClockDomain(mii.TX.CLK)
    val rxCd = ClockDomain(mii.RX.CLK)

    val macParam = MacEthParameter(
      phy = PhyParameter(
        txDataWidth = 4,
        rxDataWidth = 4
      ),
      rxDataWidth = 32,
      rxBufferByteSize = 8*1024,
      txDataWidth = 32,
      txBufferByteSize = 4*1024
    )

    soc plug new ClockingArea(soc.systemClockDomain) {
      val mac = Apb3MacEth(macParam, txCd, rxCd)
      txCd.copy(reset = mac.mac.txReset) on {
        val interframeGen = MacTxInterFrame(dataWidth = 4)
        interframeGen.io.input << mac.io.phy.tx
        mii.TX.EN := RegNext(interframeGen.io.output.valid)
        mii.TX.D := RegNext(interframeGen.io.output.data)
      }
      rxCd on {
        mac.io.phy.rx << mii.RX.toRxFlow().toStream
      }
      soc.apbSlaves += mac.io.bus -> (addrOffset, 64 KiB)
    }
  }
}


class TRNGPeripheral(val addrOffset: BigInt = 0x50000) extends Peripheral[PQVexRiscv]{
  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {
      val source = Apb3EntropySource(Apb3EntropySource.getApb3Config)
      soc.apbSlaves += source.io.bus -> (addrOffset, 64 KiB)
    }
  }
}
