package ap5

import spinal.core._
import spinal.lib._
import spinal.lib.fsm.StateMachine
import spinal.lib.fsm.EntryPoint
import spinal.lib.fsm.State
import spinal.core.Formal._

case class Command(size: Int) extends Bundle {
  /* 0: reset
   * 1: acc (data, index)
   * 2: read (index)
   */
  val op = UInt(2 bits)
  val data = UInt(64 bits)
  val index = UInt(log2Up(size) bits)
}

/* The base bit width is 32 bits. `size` * 32 = width of accumulator
 */
class MulAcc(size: Int, sideChannel: Boolean = false) extends Component {
  assert(size > 1, "Use a plain register instead")
//  setDefinitionName("MulAcc (" + size * 32 + " bits)")
  setDefinitionName("MulAcc_" + size * 32 + "bits")

  /* Export the param */
  val size_ = size

  val io = new Bundle {
    val input = slave( Stream( Command(size)) )
    val output = master( Stream( UInt(32 bits) ) )
  }

  /* Initialize our accumulator as RAM with read/write ports */
  val counter = Counter(0, size - 1)
  /* Use this instead of counter.willOverflow. Since we are reading `valueNext` (which is kind of a sin),
   * the overflow will otherwise lag behind
   */
  val counterWillOverflow = counter.valueNext === counter.end
  val acc = Mem(UInt(32 bits), wordCount = size)
  acc.init((0 until size).map(_ => U(0xAAAA, 32 bits)))
  val write = Flow(UInt(32 bits))
  write.valid := False
  write.payload := U(0)

  acc.write(
    enable  = write.valid && !ClockDomain.isResetActive,
    address = counter.valueNext,
    data    = write.payload
  )
  val read = UInt(32 bits)
  read := acc.readAsync(
    address = counter.valueNext,
    writeFirst
  )

  /* Sane defaults */
  io.input.ready := False
  io.output.valid := False
  io.output.payload := read

  when(io.input.valid) {
    /* If we fired in the last cycle or we hadn't valid, then it must be a new command. */
    val isNewCommand = past(!io.input.valid || io.input.ready || ClockDomain.isResetActive)

    switch(io.input.op) {
      /* Reset */
      is(0) {
        when(isNewCommand) {
          counter.clear()
        } otherwise {
          counter.increment()
        }
        write.valid := True
        write.payload := U(0)
        io.input.ready.setWhen(counterWillOverflow)
      }

      /* Acc */
      is(1) {
        val addArea = new Area {
          when (isNewCommand) {
            counter.valueNext := io.input.index
          } otherwise {
            counter.increment()
          }

          /* Temporary register to store intermediates */
          val tmp = RegInit(U(0, 64 bits)).setName("addArea_tmp")
          val sum = UInt(65 bits).setName("addArea_sum")
          sum := read +^ Mux(isNewCommand, io.input.data.resized, tmp)
          write.valid := True
          write.payload := sum(31 downto 0)
          tmp := sum(64 downto 32).resized
          if (sideChannel) {
            io.input.ready.setWhen(counterWillOverflow || sum(64 downto 32).resized === U(0, 64 bits))
          } else {
            io.input.ready.setWhen(counterWillOverflow)
          }
        }
      }

      /* Read */
      is(2) {
        counter.valueNext := io.input.index
        io.output.valid := True
        io.input.ready := io.output.ready
      }
    }
  }
}

object MulAcc {
  def main(args: Array[String]) {
    /*
     * Imagine having a programming language where the easiest way
     * to simply delete a folder is to call a native command, because
     * why would one include such functionality in a standard library??
     */
    import scala.sys.process._
    assert(Process("rm -rf ./out/MulAcc").! == 0)
    assert(Process("mkdir -p ./out/MulAcc").! == 0)
    val config = SpinalConfig(
      mode = SystemVerilog,
      targetDirectory = "./out/MulAcc",
      defaultConfigForClockDomains = ClockDomainConfig(
        clockEdge = RISING,
        resetKind = SYNC,
        resetActiveLevel = HIGH
      )
    )
      .includeFormal
      .generate(new MulAcc(4))
      .printPruned()
      .printPrunedIo()
      .printUnused()
  }
}

