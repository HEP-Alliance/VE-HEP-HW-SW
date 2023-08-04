
import examples.SlowdownTest
import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.bus.amba4.axilite._
import examples._
import spinal.lib.bus.amba4.axi._

// import spinal.lib.eda.symbiflow._
import java.nio.file.Paths
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class SlowdownTestSuite extends AnyFlatSpec with Matchers {
  SlowdownTest.main(args = Array())

  "A Stream.slowdown()" should "pass formal verification" in {
    import scala.sys.process._
    assert(Process("sby -f ./src/test/resources/SlowdownTest.sby --prefix out/SlowdownTest/sby_workdir").! == 0)
  }
}
