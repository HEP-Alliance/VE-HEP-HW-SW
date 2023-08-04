package examples

import spinal.core._
import spinal.lib._
import spinal.core.Formal._

class StreamSlowdown[T <: Data](val dataType: HardType[T], factor: Int) extends Component {
  setDefinitionName("SlowdownX" + factor)

  val io = new Bundle {
    val input = slave( Stream(dataType) )
    val output = master( Stream(Vec(dataType, factor)) )
    val counter = out UInt(log2Up(factor) bit)
  }
  val counter = Counter(factor)
  io.counter := counter.value
  /* The lowest data is pass-through from the input for minimal latency */
  io.output.payload(0) := io.input.payload
  /* Then, we register-chain all the values to delay them accordingly. The data
   * advances every time the input stream fires.
   */
  for (i <- 1 until factor) {
    io.output.payload(i) := RegNextWhen(io.output.payload(i - 1), io.input.fire)
  }
  when(io.input.fire) {
    counter.increment()
  }
  /* The counter keeps track of the cycles. Every $factor cycles, we prepare for
   * releasing the stored data in an output transaction
   */
  when(counter.willOverflowIfInc) {
    /* We are valid and will fire when downstream is ready */
    io.input.ready := io.output.ready
    io.output.valid := io.input.valid
  } otherwise {
    /* We are ready and wait for an upstream valid */
    io.input.ready := True
    io.output.valid := False
  }
}

class SlowdownTest extends Component {
  setDefinitionName("SlowdownTest")

  val inPayload = Bits(4 bits)

  val slowdown = new StreamSlowdown(Bits(4 bits), 4).setName("slowdown")

  val inStream = slowdown.io.input
  val outStream = slowdown.io.output
  val counter = slowdown.io.counter

  inStream.payload := inPayload

  /* Record the history of ingoing items */
  val inHistory = RegInit(Vec(B(0, 4 bits), 4))
  when(inStream.fire) {
    inHistory.assignFromBits(inHistory ## inPayload, 15, 0)
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

      when(initstate() || past(clockDomain.isResetActive)) {
        assume(!inStream.valid)
        assert(!outStream.valid)
      } otherwise {
        /* Assume some input data */
        assume(inPayload =/= 0)
        when(inStream.fire) {
          assume(inPayload =/= past(inPayload))
        }

        /* Cover that simple transactions are possible */
        cover(inStream.fire)
        cover(outStream.fire)

        /* Assert the data is correct. The past() statement is because inHistory drags behind one cycle */
        when(past(outStream.fire)) {
          assert(past(outStream.payload) === inHistory)
        } otherwise {
          /* Even when not firing, we need to assert that the inner state is correct for the k-induction to pass */
          assert(outStream.payload(1) === inHistory(0) || counter < 1)
          assert(outStream.payload(2) === inHistory(1) || counter < 2)
          assert(outStream.payload(3) === inHistory(2) || counter < 3)
        }

        // https://zipcpu.com/formal/2018/12/28/axilite.html
        /* Assume input handshake invariants */
        when(past(inStream.isStall)) {
          assume(inStream.valid)
          assume(stable(inStream.payload))
        }

        /* Assert output handshake invariants */
        when(past(outStream.isStall)) {
          assert(outStream.valid)
          assert(stable(outStream.payload.asBits))
        }

        val coverEdgeCases = new Area {
          /* Cover that maximum throughput is possible */
          cover(inStream.fire
            && past(inStream.fire, 1)
            && past(inStream.fire, 2)
            && past(inStream.fire, 3)
            && past(inStream.fire, 4)
            && past(inStream.fire, 5))
          /* The output should fire every 4 cycles */
          cover(outStream.fire
            && !past(outStream.fire, 1)
            && !past(outStream.fire, 2)
            && !past(outStream.fire, 3)
            && past(outStream.fire, 4)
            && !past(outStream.fire, 5))

          /* Cover back-pressure and other corner cases of various intensity */
          cover(inStream.isStall)
          cover(outStream.isStall
            && past(outStream.isStall, 1)
            && past(outStream.isStall, 2)
            && past(outStream.isStall, 3)
            && past(outStream.isStall, 4))
        }
      }
    }
  }
}

object SlowdownTest {
  def main(args: Array[String]) {
    /*
     * Imagine having a programming language where the easiest way
     * to simply delete a folder is to call a native command, because
     * why would one include such functionality in a standard library??
     */
    import scala.sys.process._
    assert(Process("rm -rf ./out/SlowdownTest").! == 0)
    assert(Process("mkdir -p ./out/SlowdownTest").! == 0)
    val config = SpinalConfig(
      mode = SystemVerilog,
      targetDirectory = "./out/SlowdownTest",
      defaultConfigForClockDomains = ClockDomainConfig(
        clockEdge = RISING,
        resetKind = SYNC,
        resetActiveLevel = HIGH
      )
    )
      .includeFormal
      .generate(new SlowdownTest())
      .printPruned()
      .printPrunedIo()
      .printUnused()
  }
}
