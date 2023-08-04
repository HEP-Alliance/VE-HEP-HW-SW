package mupq
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._


class SB_LUT4(value : Int) extends BlackBox {
 addGeneric("LUT_INIT",value)
 val io = new Bundle {
  val O = out(Bool) 
  val I0 = in(Bool) 
  val I1 = in(Bool) 
  val I2 = in(Bool) 
  val I3 = in(Bool) 
 }
 noIoPrefix()
 setBlackBoxName("SB_LUT4") 
}

class ICE40_RingOsc() extends Component {
  val io = new Bundle {
    val bit = out(Bool)
  }
  val bit = Bool
  val lut = new SB_LUT4(2)
  io.bit := bit
  // lut is delay line
  lut.io.I0 := ! bit
  lut.io.I1 := False
  lut.io.I2 := False
  lut.io.I3 := False
  bit := lut.io.O
}

object Apb3EntropySource {
  def getApb3Config = Apb3Config(
    addressWidth = 16,
    dataWidth = 32,
    selWidth = 1,
    useSlaveError = false
  )
}

case class Apb3EntropySource(config: Apb3Config) extends Component {
  val io = new Bundle {
    val bus = slave(Apb3(config))
  }
  val osc = new ICE40_RingOsc
  // always keep the last 32bits sampled from the RingOsc
  val samples = Reg(Bits(32 bits))
  samples(31 downto 1) := samples(30 downto 0) 
  samples(0) := osc.io.bit

  // hook up to the apb3
  val busCtrl = Apb3SlaveFactory(io.bus)
  busCtrl.read(samples, address = 0)
}


