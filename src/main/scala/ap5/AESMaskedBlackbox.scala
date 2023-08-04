package ap5
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.misc.{Clint}
import spinal.lib.bus.misc.{BusSlaveFactoryAddressWrapper}
import spinal.lib.io._
import spinal.core.sim._
import mupq._

class AESMaskedPeripheral(val addrOffset: BigInt = 0xB0000) extends Peripheral[PQVexRiscv] {
  override def setup(soc: PQVexRiscv) = {
  }
  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {
      val aesCtrl = new Apb3AESMaskedAccelerator(config = Apb3AESMaskedAccelerator.getApb3Config)
      soc.apbSlaves += aesCtrl.io.bus -> (addrOffset, 64 KiB)
    }
  }
}

class AESMaskedBlackBox() extends BlackBox {
 val io = new Bundle {
   val clk = in(Bool)
   val reset = in(Bool)
   val enable = in(Bool)
   val pt1_payload  = in(Bits(32 bits))
   val pt1_valid    = in(Bool) 
   val pt1_ready    = out(Bool)
   val pt2_payload  = in(Bits(32 bits))
   val pt2_valid    = in(Bool) 
   val pt2_ready    = out(Bool)
   val key1_payload = in(Bits(32 bits))
   val key1_valid   = in(Bool) 
   val key1_ready   = out(Bool)
   val key2_payload = in(Bits(32 bits))
   val key2_valid   = in(Bool) 
   val key2_ready   = out(Bool)
   val ct1_payload  = out(Bits(32 bits))
   val ct1_valid    = out(Bool)
   val ct1_ready    = in(Bool)
   val ct2_payload  = out(Bits(32 bits))
   val ct2_valid    = out(Bool)
   val ct2_ready    = in(Bool)
   val m            = in(Bits(28 bits))
   val done = out(Bool)
 }
 //noIoPrefix()
 setBlackBoxName("AES_Masked")
 mapClockDomain(clock=io.clk,reset=io.reset)
 addRTLPath("src/main/resources/AES_Masked.v")
}


object Apb3AESMaskedAccelerator {
  def getApb3Config = Apb3Config(
    addressWidth = 16,
    dataWidth = 32,
    selWidth = 1,
    useSlaveError = false
  )
}

case class Apb3AESMaskedAccelerator(config: Apb3Config) extends Component {
  val io = new Bundle {
    val bus = slave(Apb3(config))
  }
  val aes = new AESMaskedBlackBox

  val enable = Reg(Bool) init(False)
  val done   = Reg(Bool) init(False)

  val key        = StreamFifo(Bits(32 bits),8)
  val plaintext  = StreamFifo(Bits(32 bits),8)
  val ciphertext = Stream(Bits(32 bits))

  val masking  = StreamFifo(Bits(28 bits),280)

  aes.io.m := masking.io.pop.payload
  masking.io.pop.ready := (!key.io.pop.valid) & (!plaintext.io.pop.valid) & enable
  // mapping the streams by hand
  // TODO: define =: operator (for Fabian)
  aes.io.pt1_payload := B(0)
  aes.io.pt1_valid := False

  aes.io.pt2_payload := B(0)
  aes.io.pt2_valid := False

  when (plaintext.io.occupancy <= U(4)) {
    aes.io.pt2_payload := plaintext.io.pop.payload
    aes.io.pt2_valid := plaintext.io.pop.valid
    plaintext.io.pop.ready := aes.io.pt2_ready
  } otherwise {
    aes.io.pt1_payload := plaintext.io.pop.payload
    aes.io.pt1_valid := plaintext.io.pop.valid
    plaintext.io.pop.ready := aes.io.pt1_ready
  }
  aes.io.key1_payload := B(0)
  aes.io.key1_valid := False

  aes.io.key2_payload := B(0)
  aes.io.key2_valid := False


  when (key.io.occupancy <= U(4)) {
    aes.io.key2_payload := key.io.pop.payload
    aes.io.key2_valid := key.io.pop.valid
    key.io.pop.ready := aes.io.key2_ready
  } otherwise {
    aes.io.key1_payload := key.io.pop.payload
    aes.io.key1_valid := key.io.pop.valid
    key.io.pop.ready := aes.io.key1_ready
  }

  aes.io.ct1_ready := False
  aes.io.ct2_ready := False
  when (aes.io.ct1_valid) {
    ciphertext.payload := aes.io.ct1_payload
    ciphertext.valid := aes.io.ct1_valid
    aes.io.ct1_ready := ciphertext.ready
  } otherwise {
    ciphertext.payload := aes.io.ct2_payload
    ciphertext.valid := aes.io.ct2_valid
    aes.io.ct2_ready := ciphertext.ready
  }

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
  masking.io.push << busCtrl.createAndDriveFlow(Bits(28 bits), address = 20).toStream
}

object AESMaskedPeripherals {
  def withAESMasked(base: () => Seq[Peripheral[PQVexRiscv]]) = 
    () => base() ++ Seq(new AESMaskedPeripheral())
}
