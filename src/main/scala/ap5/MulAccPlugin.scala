package ap5

import vexriscv._
import vexriscv.plugin._
import spinal.core._
import spinal.lib._

class MulAccPlugin(size: Int = 4, sideChannel: Boolean = false, fifoDepth: Int = 3) extends Plugin[VexRiscv] {
  object MUL_LL          extends Stageable(UInt(32 bits))
  object MUL_LH          extends Stageable(UInt(32 bits))
  object MUL_HL          extends Stageable(UInt(32 bits))
  object MUL_HH          extends Stageable(UInt(32 bits))
  object MUL             extends Stageable(Bits(64 bits))
  object IS_MUL          extends Stageable(Bool)
  object IS_MULACC       extends Stageable(Bool)
  object MULACC_OP       extends Stageable(UInt(2 bits))
  object MULACC_READ     extends Stageable(UInt(32 bits))


   override def setup(pipeline: VexRiscv): Unit = {
    import Riscv._
    import pipeline.config._

//List[(Stageable[_ <: BaseType],Any)]
    val commonActions = List(
      SRC1_CTRL                -> Src1CtrlEnum.RS,
      SRC2_CTRL                -> Src2CtrlEnum.RS,
      REGFILE_WRITE_VALID      -> True,
      BYPASSABLE_EXECUTE_STAGE -> False,
      BYPASSABLE_MEMORY_STAGE  -> False,
      RS1_USE                  -> True,
      RS2_USE                  -> True
    )

    val resetActions = commonActions ++ List(MULACC_OP -> U(0,2 bits), IS_MULACC -> True) 
    val addActions   = commonActions ++ List(MULACC_OP -> U(1,2 bits), IS_MULACC -> True)
    val readActions  = commonActions ++ List(MULACC_OP -> U(2,2 bits), IS_MULACC -> True)
    val mulActions   = commonActions ++ List(IS_MUL -> True)

    // r type instructions
    def MULACC_RESET = M"1111111----------111-----0001011"
    def MULACC_READ  = M"1111111----------101-----0001011"
    def MULACC_ADD   = M"1111111----------110-----0001011"

    val decoderService = pipeline.service(classOf[DecoderService])
    decoderService.addDefault(IS_MUL, False)
    decoderService.addDefault(IS_MULACC, False)
    decoderService.addDefault(MULACC_OP, U(0,2 bits))
    decoderService.add(List(
      MULX  -> mulActions,
      MULACC_RESET -> resetActions,
      MULACC_ADD   -> addActions,
      MULACC_READ  -> readActions
    )) 
  }
 
  override def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    val mulAcc = new MulAcc(size,sideChannel=true)
    val lastMulResult = Reg(UInt(64 bits)) init(1337)
    // val cmds: Stream[Command] = null
    mulAcc.io.output.ready := False
    // fifo
    val mulCommandFifo = StreamFifo(Command(size),fifoDepth)
    // plumbing
    val cmds = mulCommandFifo.io.push
    mulCommandFifo.io.pop >> mulAcc.io.input
    cmds.valid  := False
    cmds.data   := lastMulResult
    cmds.op     := U(0).resized
    cmds.index  := U(0).resized

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

      insert(MUL_LL) := aLow * bLow
      insert(MUL_LH) := aLow * bHigh
      insert(MUL_HL) := aHigh * bLow
      insert(MUL_HH) := aHigh * bHigh


      arbitration.haltItself setWhen(arbitration.isValid && input(IS_MULACC) && (!cmds.ready))
      when(input(IS_MULACC) && arbitration.isValid) {
        cmds.op := input(MULACC_OP)
        cmds.index := input(SRC2).asUInt.resized
        cmds.valid := arbitration.isFiring
      }
    }

    memory plug new Area {
      import memory._

      val ll = UInt(32 bits)
      val lh = UInt(33 bits)
      val hl = UInt(32 bits)
      val hh = UInt(32 bits)

      ll := input(MUL_LL)
      lh := input(MUL_LH).resized
      hl := input(MUL_HL)
      hh := input(MUL_HH)

      val hllh = lh + hl
      val mul = ((hh ## ll(31 downto 16)).asUInt + hllh) ## ll(15 downto 0)
      insert(MUL) := mul
      insert(MULACC_READ) := U(0, 32 bits)
      when(input(IS_MUL)) {
        lastMulResult := mul.asUInt
      }
      arbitration.haltItself setWhen ( input(IS_MULACC) && input(MULACC_OP) === U(2, 2 bits) && !(mulAcc.io.output.valid) && arbitration.isValid)
      when(input(IS_MULACC) && input(MULACC_OP) === U(2, 2 bits) && arbitration.isValid) { // we block on the read command
        when(mulAcc.io.output.valid) {
          insert(MULACC_READ):= mulAcc.io.output.payload
          mulAcc.io.output.ready := arbitration.isFiring
        }
      }
    }

    writeBack plug new Area {
      import writeBack._

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
      // handle the read
     when(arbitration.isValid && input(MULACC_OP) === U(2, 2 bits)) {
        output(REGFILE_WRITE_DATA) := input(MULACC_READ).asBits
     }
    }
  }
}
