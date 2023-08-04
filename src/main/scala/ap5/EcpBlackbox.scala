package ap5

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._


class PCSCLKDIV extends BlackBox {
  val io = new Bundle {
    val CLKI = in(Bool)
    val RST = in(Bool)
    val SEL2 = in(Bool)
    val SEL1 = in(Bool)
    val SEL0 = in(Bool)
    val CDIV1 = out(Bool)
    val CDIVX = out(Bool)
  }
 noIoPrefix()
}

class DP1x16KD extends BlackBox {
  // one day someone probably wants to controll this but right now I want zeroed memory
  for (i <- 0 to 0xf) {
    addGeneric("INITVAL_0"+(i.toHexString.toUpperCase),B("320'b"+"0"*320))
  }
  for (i <- 0x10 to 0x3f) {
    addGeneric("INITVAL_"+(i.toHexString.toUpperCase),B("320'b"+"0"*320))
  }
  addGeneric("DATA_WIDTH_A",1)
  addGeneric("DATA_WIDTH_B",1)
  addGeneric("CLKAMUX","CLKA")
  addGeneric("CLKBMUX","CLKB")
  addGeneric("WRITEMODE_A","WRITETHROUGH")
  addGeneric("WRITEMODE_B","READBEFOREWRITE")
  addGeneric("GSR","AUTO")
  val io = new Bundle {
    // ADA0 - ADA13
    // ADB0 - ADB13
    // DIA1
    val ADA0   = in(Bool)
    val ADA1   = in(Bool)
    val ADA2   = in(Bool)
    val ADA3   = in(Bool)
    val ADA4   = in(Bool)
    val ADA5   = in(Bool)
    val ADA6   = in(Bool)
    val ADA7   = in(Bool)
    val ADA8   = in(Bool)
    val ADA9   = in(Bool)
    val ADA10  = in(Bool)
    val ADA11  = in(Bool)
    val ADA12  = in(Bool)
    val ADA13  = in(Bool)
    val ADB0   = in(Bool)
    val ADB1   = in(Bool)
    val ADB2   = in(Bool)
    val ADB3   = in(Bool)
    val ADB4   = in(Bool)
    val ADB5   = in(Bool)
    val ADB6   = in(Bool)
    val ADB7   = in(Bool)
    val ADB8   = in(Bool)
    val ADB9   = in(Bool)
    val ADB10  = in(Bool)
    val ADB11  = in(Bool)
    val ADB12  = in(Bool)
    val ADB13  = in(Bool)
    val DIA0   = in(Bool)
    val DIA1   = in(Bool)
    val DIA2   = in(Bool)
    val DIA3   = in(Bool)
    val DIA4   = in(Bool)
    val DIA5   = in(Bool)
    val DIA6   = in(Bool)
    val DIA7   = in(Bool)
    val DIA8   = in(Bool)
    val DIA9   = in(Bool)
    val DIA10  = in(Bool)
    val DIA11  = in(Bool)
    val DIA12  = in(Bool)
    val DIA13  = in(Bool)
    val DIA14  = in(Bool)
    val DIA15  = in(Bool)
    val DIA16  = in(Bool)
    val DIA17  = in(Bool)
    val DOB0 = out(Bool)
    // from bram_map.v
    val CLKA = in(Bool) // CLK2
    val CLKB = in(Bool) // CLK3
    val WEA  = in(Bool) // |A1EN
    val CEA  = in(Bool) // 1'b1
    val OCEA = in(Bool) // 1'b1
    val WEB  = in(Bool) // 1'b0
    val CEB  = in(Bool) // B1EN 
    val OCEB = in(Bool) // 1'b1
    val RSTA = in(Bool) // 1'b0
    val RSTB = in(Bool) // 1'b0
  }
 noIoPrefix()
 mapCurrentClockDomain(io.CLKA)
 mapCurrentClockDomain(io.CLKB)
 setBlackBoxName("DP16KD")
 // WEA and CEB are the enables for the two ports
 def assignDefaults = {
   io.CEA  := True
   io.OCEA := True
   io.WEB  := False
   io.OCEB := True
   io.RSTA := False
   io.RSTB := False
 }
 

//use with map to create array of outputs
 def connect(bus :PipelinedMemoryBus, index: Int): Bool = {
  val mask                    = bus.cmd.mask
  io.WEA := bus.cmd.write && bus.cmd.valid && !mask(index/8)
  io.CEB := bus.cmd.valid 
  io.DIA0 := bus.cmd.data(index)
  io.ADA0 := bus.cmd.address(0)  
  io.ADA1 := bus.cmd.address(1)
  io.ADA2 := bus.cmd.address(2)
  io.ADA3 := bus.cmd.address(3)
  io.ADA4 := bus.cmd.address(4)
  io.ADA5 := bus.cmd.address(5)
  io.ADA6 := bus.cmd.address(6)
  io.ADA7 := bus.cmd.address(7)
  io.ADA8 := bus.cmd.address(8)
  io.ADA9 := bus.cmd.address(9)
  io.ADA10:= bus.cmd.address(10)
  io.ADA11:= bus.cmd.address(11)
  io.ADA12:= bus.cmd.address(12)
  io.ADA13:= bus.cmd.address(13)
  io.ADB0 := bus.cmd.address(0)
  io.ADB1 := bus.cmd.address(1)
  io.ADB2 := bus.cmd.address(2)
  io.ADB3 := bus.cmd.address(3)
  io.ADB4 := bus.cmd.address(4)
  io.ADB5 := bus.cmd.address(5)
  io.ADB6 := bus.cmd.address(6)
  io.ADB7 := bus.cmd.address(7)
  io.ADB8 := bus.cmd.address(8)
  io.ADB9 := bus.cmd.address(9)
  io.ADB10:= bus.cmd.address(10)
  io.ADB11:= bus.cmd.address(11)
  io.ADB12:= bus.cmd.address(12)
  io.ADB13:= bus.cmd.address(13)
  io.DIA1  := False 
  io.DIA2  := False 
  io.DIA3  := False 
  io.DIA4  := False 
  io.DIA5  := False 
  io.DIA6  := False 
  io.DIA7  := False 
  io.DIA8  := False 
  io.DIA9  := False 
  io.DIA10 := False 
  io.DIA11 := False 
  io.DIA12 := False 
  io.DIA13 := False 
  io.DIA14 := False 
  io.DIA15 := False 
  io.DIA16 := False 
  io.DIA17 := False 
  return io.DOB0
 }
}



class QUADSPI extends BlackBox { 
val io = new Bundle { 
  val SCLK = in(Bool)
  val CS   = in(Bool)
  val SI   = inout(Bool)
  val SO   = inout(Bool)
  val WP   = inout(Bool)
  val SIO3 = inout(Bool)
}
noIoPrefix()
//mapClockDomain(clock=io.clk,reset=io.rst)
addRTLPath("src/main/resources/MX25L12833F.v")
setBlackBoxName("quad_spi")
}
