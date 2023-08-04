package ap5
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.misc.{Clint}
import spinal.lib.bus.misc.{BusSlaveFactoryAddressWrapper}
import spinal.lib.io._
import spinal.core.sim._
import mupq._

class AESPeripheral(val addrOffset: BigInt = 0xB0000) extends Peripheral[PQVexRiscv] {
  override def setup(soc: PQVexRiscv) = {
  }
  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {
      val aesCtrl = new Apb3AESAccelerator(config = Apb3AESAccelerator.getApb3Config)
      soc.apbSlaves += aesCtrl.io.bus -> (addrOffset, 64 KiB)
    }
  }
}

class AESBlackBox() extends BlackBox {
 val io = new Bundle {
   val clk = in(Bool)
   val reset = in(Bool)
   val enable = in(Bool)
   val plaintext_payload = in(Bits(32 bits))
   val plaintext_valid   = in(Bool) 
   val plaintext_ready   = out(Bool)
   val key_payload = in(Bits(32 bits))
   val key_valid   = in(Bool) 
   val key_ready   = out(Bool)
   val ciphertext_payload = out(Bits(32 bits))
   val ciphertext_valid   = out(Bool) 
   val ciphertext_ready   = in(Bool)
   val done = out(Bool)
 }
 //noIoPrefix()
 setBlackBoxName("AES")
 mapClockDomain(clock=io.clk,reset=io.reset)
 addRTLPath("src/main/resources/AES.v")
}


object Apb3AESAccelerator {
  def getApb3Config = Apb3Config(
    addressWidth = 16,
    dataWidth = 32,
    selWidth = 1,
    useSlaveError = false
  )
}

case class Apb3AESAccelerator(config: Apb3Config) extends Component {
  val io = new Bundle {
    val bus = slave(Apb3(config))
  }
  val aes = new AESBlackBox

  val enable = Reg(Bool) init(False)
  val done   = Reg(Bool) init(False)

  val key        = StreamFifo(Bits(32 bits),4)
  val plaintext  = StreamFifo(Bits(32 bits),4)
  val ciphertext = Stream(Bits(32 bits))

  // mapping the streams by hand
  // TODO: define =: operator (for Fabian)
  aes.io.key_payload := key.io.pop.payload
  aes.io.key_valid := key.io.pop.valid
  key.io.pop.ready := aes.io.key_ready 

  aes.io.plaintext_payload := plaintext.io.pop.payload
  aes.io.plaintext_valid := plaintext.io.pop.valid
  plaintext.io.pop.ready := aes.io.plaintext_ready 

  ciphertext.payload := aes.io.ciphertext_payload
  ciphertext.valid := aes.io.ciphertext_valid
  aes.io.ciphertext_ready := ciphertext.ready

  aes.io.enable := enable
  when(aes.io.done) {
    enable := False
    aes.io.enable := False
    done := True
  }
  when (aes.io.enable) {
    done := False
  }

  // hook up to the apb3
  // [STATUS][CTRL][KEY][PLAIN][CIPHER]
  val busCtrl = Apb3SlaveFactory(io.bus)
  busCtrl.read(done, address = 0)
  busCtrl.write(enable, address = 4)

  key.io.push << busCtrl.createAndDriveFlow(Bits(32 bits), address = 8).toStream
  plaintext.io.push << busCtrl.createAndDriveFlow(Bits(32 bits), address = 12).toStream
  ciphertext.ready := False
  //busCtrl.readStreamNonBlocking(ciphertext,address = 16)
  // this is sooooo fugly
  busCtrl.read(ciphertext.payload, address = 16)
  when (io.bus.PADDR === 0x0010 && io.bus.PENABLE) {
    ciphertext.ready := True
  }
}

object AESPeripherals {
  def withAES(base: () => Seq[Peripheral[PQVexRiscv]]) = 
    () => base() ++ Seq(new AESPeripheral())
}
