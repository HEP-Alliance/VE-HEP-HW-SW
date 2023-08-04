package ap5

import scopt.OptionParser
import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._

import vexriscv._
import vexriscv.plugin._

import mupq._


case class PipelinedMemoryBusRam() extends Component {
  val busConfig = PipelinedMemoryBusConfig(log2Up(64<<10), 32)
  val io = new Bundle {
    val bus = slave(PipelinedMemoryBus(busConfig))
  }

  val ram = Mem(Bits(32 bits), 16<<10)
  io.bus.rsp.valid := Delay(io.bus.cmd.fire && !io.bus.cmd.write, 2, init = False)
  val rdata = ram.readWriteSync(
    address = io.bus.cmd.address >> 2,
    data = io.bus.cmd.data,
    enable = io.bus.cmd.valid,
    write = io.bus.cmd.write,
    mask = io.bus.cmd.mask
  )
  io.bus.rsp.data := RegNext(rdata) init (0)
  io.bus.cmd.ready := True
}


case class PipelinedMemoryBusiECP5() extends Component {
  val busConfig = PipelinedMemoryBusConfig(log2Up(64<<10),32);
  val io = new Bundle {
    val bus = slave(PipelinedMemoryBus(busConfig))
  }
  io.bus.rsp.valid := Delay(io.bus.cmd.fire && !io.bus.cmd.write, 2, init = False)
  for (i <- (0 to 31)) {
     val ram = Mem(Bool, 16 <<10)
     val rdata = ram.readWriteSync(
      address = (io.bus.cmd.address >> 2),
      data = io.bus.cmd.data(i),
      enable = io.bus.cmd.valid,
      write = io.bus.cmd.write && io.bus.cmd.mask(i/8,1 bits).asBool
      //mask = io.bus.cmd.mask(i/8,1 bits)
     )
     io.bus.rsp.data(i) := RegNext(rdata) init(False)
  }
  // apparently we need a Delay of 2 here 
  io.bus.cmd.ready := True
}


case class PipelinedMemoryBusVEHEP() extends Component {
  val busConfig = PipelinedMemoryBusConfig(log2Up(32<<10),32);
  val io = new Bundle {
    val clk = in(Bool)
    val bus = slave(PipelinedMemoryBus(busConfig))
  }
  io.bus.rsp.valid := Delay(io.bus.cmd.fire && !io.bus.cmd.write, 2, init = False)
  val sram = new SramModule()
  sram.io.A_CLK := io.clk
  sram.io.A_ADDR := (io.bus.cmd.address>>2).asBits
  sram.io.A_MEN := True
  sram.io.A_WEN := io.bus.cmd.write
  sram.io.A_REN := !io.bus.cmd.write
  sram.io.A_DIN := io.bus.cmd.data
  sram.io.A_DLY := True
  for (i <- (0 to 31)) {
    sram.io.A_BM(i) := io.bus.cmd.mask(i/8,1 bits).asBool
  }
  //sram.io.A_BM := B(0xffffffffL)
  io.bus.rsp.data := RegNext(sram.io.A_DOUT) init(B(0))
  io.bus.cmd.ready := True

  sram.io.A_BIST_CLK := io.clk;
  sram.io.A_BIST_MEN := False;
  sram.io.A_BIST_EN := False;
  sram.io.A_BIST_WEN := False;
  sram.io.A_BIST_REN := False;
  sram.io.A_BIST_ADDR := B(0)
  sram.io.A_BIST_DIN := B(0)
  sram.io.A_BIST_BM := B(0);
}

class PQVexRiscvVEHEP(
  val coreFrequency: HertzNumber = 12 MHz,
  cpuPlugins: () => Seq[Plugin[VexRiscv]] = PQVexRiscv.withDSPMultiplier(),
  apbPeripherals: () => Seq[Peripheral[PQVexRiscv]] = PQVexRiscv.withUart(),
  ecp5: Boolean
)
extends PQVexRiscv(
  cpuPlugins = cpuPlugins,
  ibusRange = SizeMapping(0x80000000L, 384 KiB),
  apbPeripherals = apbPeripherals
) {
  val io = new Bundle {
    val clk = in Bool()
    val reset = in Bool()
  }
  mainClock := io.clk
  asyncReset := io.reset
  noIoPrefix()
  val memory = new ClockingArea(systemClockDomain) {
      if (ecp5 == false) {
        for (i <- 0 to 10) {
          val ram1 = PipelinedMemoryBusVEHEP()
          ram1.io.clk := io.clk
          busSlaves += ram1.io.bus -> SizeMapping(0x80000000L+((32 KiB)*i), 32 KiB)
        }
      } else {
        for (i <- 0 to 5) {
          val ram1 = PipelinedMemoryBusiECP5()
          busSlaves += ram1.io.bus -> SizeMapping(0x80000000L+((64 KiB)*i), 64 KiB)
        }
      }
  }
}

object PQVexRiscvVEHEP {
  type PluginSeq = Seq[Plugin[VexRiscv]]
  type PluginGen = () => PluginSeq

  def configBase(base: PluginGen = () => Seq()) = () =>
    base() ++ Seq(
      new IBusSimplePlugin(
        resetVector = 0x80000000L,
        cmdForkOnSecondStage = true,
        cmdForkPersistence = false,
        prediction = NONE,
        catchAccessFault = true,
        compressedGen = false
      ),
      new DBusSimplePlugin(
        catchAddressMisaligned = true,
        catchAccessFault = true,
        earlyInjection = true
      ),
      new CsrPlugin(
        CsrPluginConfig
          .smallest(0x80000000L)
          .copy(
            mtvecAccess = CsrAccess.READ_WRITE,
            mcycleAccess = CsrAccess.READ_ONLY,
            minstretAccess = CsrAccess.READ_ONLY
          )
      ),
      new DecoderSimplePlugin(
        catchIllegalInstruction = true
      ),
      new RegFilePlugin(
        regFileReadyKind = plugin.SYNC,
        zeroBoot = false
      ),
      new IntAluPlugin,
      new SrcPlugin(
        separatedAddSub = false,
        executeInsertion = false
      ),
      new FullBarrelShifterPlugin,
      new HazardSimplePlugin(
        bypassExecute = true,
        bypassMemory = true,
        bypassWriteBack = true,
        bypassWriteBackBuffer = true,
        pessimisticUseSrc = false,
        pessimisticWriteRegFile = false,
        pessimisticAddressMatch = false
      ),
      new BranchPlugin(
        earlyBranch = false,
        catchAddressMisaligned = false
      ),
      new YamlPlugin("cpu0.yaml")
    )

  def main(args: Array[String]): Unit = {
    case class PQVexRiscvVEHEPConfig(
      cpuPlugins: () => Seq[Plugin[VexRiscv]] = configBase(),
      apbPeripherals: () => Seq[Peripheral[PQVexRiscv]] = PQVexRiscv.withUart(),
      ecp5: Boolean = true
    )
    val optParser = new OptionParser[PQVexRiscvVEHEPConfig]("PQVexRiscvUP5K") {
      head("PQVexRiscvVEHEP board")
      help("help") text ("print usage text")
      opt[Unit]("spi") action ((_, c) =>
        c.copy(apbPeripherals = SpiPeripherals.withSpiSlave(base = c.apbPeripherals)))
      opt[Unit]("aes") action ((_, c) =>
        c.copy(apbPeripherals = AESPeripherals.withAES(base = c.apbPeripherals)))
      opt[Unit]("mulacc") action ((_, c) =>
        c.copy(cpuPlugins = () => c.cpuPlugins() ++ Seq(new CombaPlugin, new MulDivIterativePlugin(genMul=false,genDiv=true))))
      opt[Unit]("aes-masked") action ((_, c) =>
        c.copy(apbPeripherals = AESMaskedPeripherals.withAESMasked(base = c.apbPeripherals)))
      opt[Unit]("gpio") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withGpio(gpioWidth = 32, base = c.apbPeripherals)))
      opt[Unit]("ecp5") action ((_, c) =>
        c.copy(ecp5 = true))
    }
    val config = optParser.parse(args, PQVexRiscvVEHEPConfig()) match {
      case Some(config) => config
      case None         => ???
    }
    val report = SpinalConfig(
      mode = Verilog,
      targetDirectory = "./out/"
    ).generate(
      new PQVexRiscvVEHEP(
        cpuPlugins = config.cpuPlugins,
        apbPeripherals = config.apbPeripherals,
        ecp5 = config.ecp5
      )
    )
    report.mergeRTLSource(s"rtl/gen/${report.toplevelName}.aux")
  }
}

