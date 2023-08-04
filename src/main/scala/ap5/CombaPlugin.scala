package ap5

import vexriscv._
import vexriscv.plugin._
import spinal.core._


class KaraMult(width: Int) extends Component{
    val io = new Bundle{
        val a = in Bits(width bits)
        val b = in Bits(width bits)
        val result = out Bits(2*width bits)
    }

    if (width <= 32){
        io.result := (io.a.asUInt*io.b.asUInt).asBits.resize(width*2)
    } else {
        // Splitting a and b into two parts with length width/2 each (high and low)
        val al = io.a(0 to ((width/2)-1))
        val ah = io.a((width/2) to (width-1))
        val bl = io.b(0 to ((width/2)-1))
        val bh = io.b((width/2) to (width-1))

        // Calc ahl and bhl as signed integers
        val alh_int = (al.resize(width/2+1).asSInt - ah.resize(width/2+1).asSInt)
        val bhl_int = (bh.resize(width/2+1).asSInt - bl.resize(width/2+1).asSInt)

        // Recursions
        val p1 = new KaraMult(width/2)
        p1.io.a := ah
        p1.io.b := bh
        val rh = p1.io.result

        val p2 = new KaraMult(width/2)
        p2.io.a := al
        p2.io.b := bl
        val rl = p2.io.result

        val p3 = new KaraMult(width/2)
        p3.io.a := alh_int.abs.asBits.resize(width/2)
        p3.io.b := bhl_int.abs.asBits.resize(width/2)
        val rm = p3.io.result

        // Calc the results from the recursions and return them
        val result_m =  Bits ((width + (width/2) + 2) bits)
        when((alh_int < 0) ^ (bhl_int < 0)){
            result_m := (((rh.asUInt +^ rl.asUInt - rm.asUInt).asBits) << width/2).resized
        }.otherwise {
            result_m := (((rh.asUInt +^ rl.asUInt +^ rm.asUInt).asBits) << width/2).resized
        }
        val result_h = rh << width
        val result_l = rl

        io.result := (result_h.asUInt +^ result_m.asUInt +^ result_l.asUInt).asBits.resize(width*2)
    }
}



/**
  * A multiplication plugin using only 16-bit multiplications
  */
class CombaPlugin extends Plugin[VexRiscv]{

  object MUL_LL extends Stageable(UInt(32 bits))
  object MUL_LH extends Stageable(UInt(32 bits))
  object MUL_HL extends Stageable(UInt(32 bits))
  object MUL_HH extends Stageable(UInt(32 bits))

  object MUL     extends Stageable(Bits(64 bits))

  object IS_MUL  extends Stageable(Bool)
  object IS_COMBA extends Stageable(Bool)
  object IS_OP_SHIFT  extends Stageable(Bool)
  object IS_OP_MUL  extends Stageable(Bool)
  object IS_OP_ADD  extends Stageable(Bool)
  object IS_OP_SUB  extends Stageable(Bool)

  override def setup(pipeline: VexRiscv): Unit = {
    import Riscv._
    import pipeline.config._


    val actions = List[(Stageable[_ <: BaseType],Any)](
      SRC1_CTRL                -> Src1CtrlEnum.RS,
      SRC2_CTRL                -> Src2CtrlEnum.RS,
      REGFILE_WRITE_VALID      -> True,
      BYPASSABLE_EXECUTE_STAGE -> False,
      BYPASSABLE_MEMORY_STAGE  -> False,
      RS1_USE                  -> True,
      RS2_USE                  -> True,
      IS_MUL                   -> True
    )


    val commonActions = List(
      SRC1_CTRL                -> Src1CtrlEnum.RS,
      SRC2_CTRL                -> Src2CtrlEnum.RS,
      REGFILE_WRITE_VALID      -> True,
      BYPASSABLE_EXECUTE_STAGE -> False,
      BYPASSABLE_MEMORY_STAGE  -> False,
      RS1_USE                  -> True,
      RS2_USE                  -> True
    )

    val opshiftActions = commonActions ++ List(IS_OP_SHIFT -> True, IS_COMBA -> True) 
    val opmulActions   = commonActions ++ List(IS_OP_MUL   -> True, IS_COMBA -> True)
    val opaddActions  = commonActions ++ List(IS_OP_ADD -> True, IS_COMBA -> True)
    val opsubActions  = commonActions ++ List(IS_OP_SUB -> True, IS_COMBA -> True)
    val mulActions   = commonActions ++ List(IS_MUL -> True)



    // r type instructions
    def MULACC_SHIFT = M"1111111----------111-----0001011"
    def MULACC_MUL   = M"1111111----------101-----0001011"
    def MULACC_ADD   = M"1111111----------110-----0001011"
    def MULACC_SUB   = M"1111111----------100-----0001011"


    val decoderService = pipeline.service(classOf[DecoderService])
    decoderService.addDefault(IS_MUL, False)
    decoderService.addDefault(IS_COMBA, False)
    decoderService.addDefault(IS_OP_SHIFT, False)
    decoderService.addDefault(IS_OP_MUL, False)
    decoderService.addDefault(IS_OP_ADD, False)
    decoderService.addDefault(IS_OP_SUB, False)
    decoderService.add(List(
      MULX  -> mulActions,
      MULACC_SHIFT -> opshiftActions,
      MULACC_MUL   -> opmulActions,
      MULACC_ADD  -> opaddActions,
      MULACC_SUB  -> opsubActions
    ))

  }

  override def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    // Prepare signed inputs for the multiplier in the next stage.
    // This will map them best to an FPGA DSP.
    execute plug new Area {
      import execute._
      val a,b = Bits(32 bit)

      a := input(SRC1)
      b := input(SRC2)

      val aLow = a(15 downto 0).asUInt
      val bLow = b(15 downto 0).asUInt
      val aHigh = a(31 downto 16).asUInt
      val bHigh = b(31 downto 16).asUInt
//
      insert(MUL_LL) := aLow * bLow
      insert(MUL_LH) := aLow * bHigh
//      insert(MUL_LH) := (aHigh+bLow) * (bHigh+aLow)
      insert(MUL_HL) := aHigh * bLow
      insert(MUL_HH) := aHigh * bHigh

//    val kara = new KaraMult(32)
//    kara.io.a := a 
//    kara.io.b := b
//    insert(MUL):= (a.asUInt*b.asUInt).asBits


    }

    memory plug new Area {
      import memory._

      val ll = UInt(32 bits)
      val lh = UInt(33 bits)
      val hl = UInt(32 bits)
      val hh = UInt(32 bits)
//
      ll := input(MUL_LL)
      lh := input(MUL_LH).resized
      hl := input(MUL_HL)
      hh := input(MUL_HH)
//
      val hllh = lh + hl
      insert(MUL) := ((hh ## ll(31 downto 16)).asUInt + hllh) ## ll(15 downto 0)
//      insert(MUL) := ((hh<<32) + ((lh - (ll+hh))<<16) + ll).asBits
    }

    writeBack plug new Area {
      import writeBack._

      val acc =  Reg(UInt(72 bits)) init(0)
      val invalue = UInt(64 bits)
      val sum = UInt(72 bits)
      invalue := 0
      sum := acc+invalue

      when(input(IS_COMBA) && arbitration.isFiring) {
        when(input(IS_OP_MUL)){
          invalue := input(MUL)(63 downto 0).resized.asUInt
        }
        when(input(IS_OP_ADD)){
          invalue := (input(SRC2)(31 downto 0) ## input(SRC1)(31 downto 0)).asUInt
        }
        when(input(IS_OP_SUB)){ // that's subtraction
          invalue := (~input(SRC2)(31 downto 0) ## ~input(SRC1)(31 downto 0)).asUInt + 1
        }
        when(input(IS_OP_SHIFT)) {
          acc := (sum >> 32).resized
          output(REGFILE_WRITE_DATA) := (sum(31 downto 0) >> (input(SRC1)(3 downto 0)).asUInt).asBits.resized
        }.otherwise {
          acc := sum
        }
      }
      val aSigned,bSigned = Bool
      switch(input(INSTRUCTION)(13 downto 12)) {
        is(B"01") {
          aSigned := True
          bSigned := True
        }
        is(B"10") {
          aSigned := True
          bSigned := False
        }
        default {
          aSigned := False
          bSigned := False
        }
      }

      val a = (aSigned && input(SRC1).msb) ? input(SRC2).asUInt | U(0)
      val b = (bSigned && input(SRC2).msb) ? input(SRC1).asUInt | U(0)

      when(arbitration.isValid && input(IS_MUL)){
        switch(input(INSTRUCTION)(13 downto 12)){
          is(B"00"){
            output(REGFILE_WRITE_DATA) := input(MUL)(31 downto 0)
          }
          is(B"01",B"10",B"11"){
            output(REGFILE_WRITE_DATA) := (((input(MUL)(63 downto 32)).asUInt + ~a) + (~b + 2)).asBits
          }
        }
      }
    }
  }
}
