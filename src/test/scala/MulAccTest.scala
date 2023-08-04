// for deleting directories


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import spinal.core._
import spinal.lib._
import spinal.core.Formal._
import ap5.{MulAcc,Command}

class MulAccVerifier(size: Int, sideChannel: Boolean = false) extends Component {
  setDefinitionName("MulAccVerifier")
  val dut = new MulAcc(size, sideChannel)

  /* Re-export for typing convenience */
  val io = new Bundle {
    val input = dut.io.input
    val output = dut.io.output
  }

  GenerationFlags.formal {
    ClockDomain.current.withoutReset() {
      /* Init stuff */
      when(initstate()) {
        assume(clockDomain.isResetActive)
      }
      /* Start things up */
      cover(!initstate())
      cover(!clockDomain.isResetActive)
    }

    /* Assume input handshake invariants */
    when(past(io.input.isStall) && past(!clockDomain.isResetActive)) {
      assume(io.input.valid)
      assume(stable(io.input.payload.asBits))
    }

    /* Assert output handshake invariants */
    when(past(io.output.isStall) && past(!clockDomain.isResetActive)) {
      assert(io.output.valid)
      assert(stable(io.output.payload.asBits))
    }

    /* Drive the DUT's input */
    io.input.op.addAttribute("anyseq")
    assume(io.input.op < 3)
    io.input.data.addAttribute("anyseq")
    io.input.index.addAttribute("anyseq")
    assume(io.input.index <= size - 2)

    /* Drive the DUT's output */
    io.output.ready.addAttribute("anyseq")
  }

  /* The first command issued after a reset must be `reset` */
  val needsReset = RegInit(True)
  when(needsReset && io.input.valid) {
    GenerationFlags.formal {
      assume(io.input.op === U(0, 2 bits))
    }
    needsReset.clear()
  }

  /* Cover some interesting cases */
  GenerationFlags.formal {
    cover(io.input.fire && io.input.op === U(0, 2 bits))
    cover(io.input.fire && io.input.op === U(1, 2 bits))
    cover(io.input.fire && io.input.op === U(2, 2 bits))

    /* Cover that simple transactions are possible */
    cover(io.input.fire)
    cover(io.output.fire)

    val coverEdgeCases = new Area {
      /* Cover that maximum throughput while reading is possible */
      cover(
        io.input.fire && io.input.op === U(2, 2 bits)
        && past(io.input.fire && io.input.op === U(2, 2 bits), 1)
        && past(io.input.fire && io.input.op === U(2, 2 bits), 2)
        && past(io.input.fire && io.input.op === U(2, 2 bits), 3)
        && past(io.input.fire && io.input.op === U(2, 2 bits), 4)
        && past(io.input.fire && io.input.op === U(2, 2 bits), 5)
      )
      /* We also want to cove a read every two cycles because it triggers a different state machine path */
      cover(
        io.input.fire && io.input.op === U(2, 2 bits)
          && !past(io.input.fire, 1)
          && past(io.input.fire && io.input.op === U(2, 2 bits), 2)
          && !past(io.input.fire, 3)
          && past(io.input.fire && io.input.op === U(2, 2 bits), 4)
          && !past(io.input.fire, 5)
      )
      /* Cover three consecutive resets (though that is a bit stupid to do) */
      cover(
        io.input.fire && io.input.op === U(0, 2 bits)
          && past(io.input.fire && io.input.op === U(0, 2 bits), size)
          && past(io.input.fire && io.input.op === U(0, 2 bits), 2 * size)
      )
      /* Cover a read-reset-acc sequence with maximum throughput */
      cover(
        io.input.fire && io.input.op === U(1, 2 bits)
          && past(io.input.fire && io.input.op === U(0, 2 bits), size)
          && past(io.input.fire && io.input.op === U(2, 2 bits), 2 * size)
      )
      /* Cover a reset acc read sequence with constant throughput */
      cover(
        io.input.fire && io.input.op === U(2, 2 bits)
          && past(io.input.fire && io.input.op === U(1, 2 bits), size)
          && past(io.input.fire && io.input.op === U(0, 2 bits), 2 * size)
      )
      /* Cover the fastest possible add sequence (2 cycles). Fun fact: this can go down to one cycle
       * in sideChannel mode (:
       */
      cover(
        io.input.fire && io.input.op === U(1, 2 bits)
          && past(io.input.fire && io.input.op === U(1, 2 bits), 2)
          && past(io.input.fire && io.input.op === U(1, 2 bits), 4)
      )
      /* Assert that an addition never takes longer than `size` cycles (negative cover)
       * i.e.: If we fire with a write, the last fire must have been at most `size` cycles ago.
       * Note that this is formulated as an implication: never(fireWithAdd => (fired at more than `size` cycles ago))
       */
      assert(!(
        (io.input.fire && io.input.op === U(1, 2 bits))
          && (1 to size).map { delay => past(io.input.isStall, delay) }.reduce(_ && _)
        ))

      /* Same for resetting */
      assert(!(
        (io.input.fire && io.input.op === U(0, 2 bits))
          && (1 to size).map { delay => past(io.input.isStall && !clockDomain.isResetActive, delay) }.reduce(_ && _)
        ))

      /* Cover back-pressure and other corner cases of various intensity */
      cover(io.input.isStall)
      cover(io.output.isStall
        && past(io.output.isStall, 1)
        && past(io.output.isStall, 2)
        && past(io.output.isStall, 3)
        && past(io.output.isStall, 4))
    }
  }

  /* One-cycle re-implementation of the DUT */
  val acc = RegInit(U(0, size * 32 bits))
  val shouldRead_1 = RegInit(False)
  val shouldRead_2 = RegInit(U(0, 32 bits))

  when(io.input.fire) {
    switch(io.input.op) {
      /* Reset */
      is(0) {
        acc := U(0)
      }
      /* Acc */
      is(1) {
        acc := acc + (io.input.data << (io.input.index << 5)).resized
      }
      /* Read */
      is(2) {
        assert(shouldRead_1 === False)
        val shouldRead = UInt(32 bits)
        shouldRead := (acc >> (io.input.index << 5)).resized
        when(io.output.fire) {
          assert(io.output.payload === shouldRead)
        } otherwise {
          /* Store the value for some time later */
          shouldRead_1 := True
          shouldRead_2 := shouldRead
        }
      }
    }
  }
  when(io.output.fire && io.input.op =/= U(2)) {
    assert(shouldRead_1)
    shouldRead_1 := False
    assert(io.output.payload === shouldRead_2)
  }
}

object MulAccVerifier {
  def main(sideChannel: Boolean = false) {
    // delete directories with scala
    import java.io.File
    import scala.reflect.io.Directory
    assert((new Directory(new File("./out/MulAccVerifier"))).deleteRecursively())
    import scala.sys.process._
    assert(Process("mkdir -p ./out/MulAccVerifier").! == 0)
    val config = SpinalConfig(
      mode = SystemVerilog,
      targetDirectory = "./out/MulAccVerifier",
      defaultConfigForClockDomains = ClockDomainConfig(
        clockEdge = RISING,
        resetKind = SYNC,
        resetActiveLevel = HIGH
      )
    )
      .includeFormal
      .generate(new MulAccVerifier(4, sideChannel))
      .printPruned()
      .printPrunedIo()
      .printUnused()
  }
}


class MulAccTest extends AnyFlatSpec with Matchers {
  "A MulAcc" should "pass bounded model check & cover check" in {
    MulAccVerifier.main()
    import scala.sys.process._
    assert(Process("sby -f ./src/test/resources/MulAccVerifier.sby --prefix out/MulAccVerifier/sby_workdir").! == 0)
  }

  "A MulAcc with side channels" should "pass bounded model check & cover check" in {
    MulAccVerifier.main(true)
    import scala.sys.process._
    assert(Process("sby -f ./src/test/resources/MulAccVerifier.sby --prefix out/MulAccVerifier/sby_workdir").! == 0)
  }
}
