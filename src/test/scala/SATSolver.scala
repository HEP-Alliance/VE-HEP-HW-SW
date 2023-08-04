
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import spinal.core._
import spinal.lib._
import spinal.core.Formal._
import scala.util.Random;

class Factorizer(width: BitCount) extends Component {
  setDefinitionName("Factorizer_" + width.value)

  val A = UInt(width).addAttribute("anyconst")
  val B = UInt(width).addAttribute("anyconst")
  val prod = A * B

  GenerationFlags.formal {
    ClockDomain.current.withoutReset() {
      var rand = new Random(0)
      val targetA = BigInt.probablePrime(A.getBitsWidth, rand)
      val targetB = BigInt.probablePrime(B.getBitsWidth, rand)
      val targetProd = targetA * targetB
      println("The desired value is: " + targetA + "*" + targetB + "=" + targetProd)
      cover(prod === targetProd)
    }
  }
}

object SATSolver {
  def main() {
    /*
     * Imaging having a programming language where the easiest way
     * to simply delete a folder is to call a native command, because
     * why would one include such functionality in a standard library??
     */
    import scala.sys.process._
    assert(Process("rm -rf ./out/SATSolver").! == 0)
    assert(Process("mkdir -p ./out/SATSolver").! == 0)
    val config = SpinalConfig(
      mode = SystemVerilog,
      targetDirectory = "./out/SATSolver",
      defaultConfigForClockDomains = ClockDomainConfig(
        clockEdge = RISING,
        resetKind = SYNC,
        resetActiveLevel = HIGH
      )
    )
      .includeFormal
      .generate(new Component {
        setDefinitionName("SATSolver")

        val fact4 = new Factorizer(4 bits)
        val fact8 = new Factorizer(8 bits)
        val fact12 = new Factorizer(12 bits)
        val fact16 = new Factorizer(16 bits)
        val fact20 = new Factorizer(20 bits)
        val fact24 = new Factorizer(24 bits)
        // val fact28 = new Factorizer(28 bits)
        // val fact32 = new Factorizer(32 bits)
      })
      .printPruned()
      .printPrunedIo()
      .printUnused()
  }
}

class SATSolver extends AnyFlatSpec with Matchers {
  SATSolver.main()

  "Formal verification tools" should "factorize numbers with 8 bits" in {
    import scala.sys.process._
    assert(Process("sby -f ./src/test/resources/SATSolver.sby --prefix out/SATSolver/sby_workdir").! == 0)
  }
}
