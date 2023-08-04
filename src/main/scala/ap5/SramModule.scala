package ap5

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._

class SramModule extends BlackBox {
  val io = new Bundle {
    val A_CLK = in(Bool)
    val A_MEN = in(Bool)
    val A_WEN = in(Bool)
    val A_REN = in(Bool)
    val A_ADDR = in(Bits(13 bits))
    val A_DIN  = in(Bits(32 bits))
    val A_DLY  = in(Bool)
    val A_DOUT = out(Bits(32 bits))
    val A_BM   = in(Bits(32 bits))
    val A_BIST_CLK = in(Bool)
    val A_BIST_MEN = in(Bool)
    val A_BIST_EN = in(Bool)
    val A_BIST_WEN = in(Bool)
    val A_BIST_REN = in(Bool)
    val A_BIST_ADDR = in(Bits(13 bits))
    val A_BIST_DIN  = in(Bits(32 bits))
    val A_BIST_BM   = in(Bits(32 bits))
  }
  noIoPrefix()
  setBlackBoxName("RM_IHPSG13_1P_8192x32_c4_bm_bist")
  addRTLPath("src/main/resources/RM_IHPSG13_1P_8192x32_c4_bm_bist.v")
  addRTLPath("src/main/resources/RM_IHPSG13_1P_core_behavioral_bm_bist.v")
}
