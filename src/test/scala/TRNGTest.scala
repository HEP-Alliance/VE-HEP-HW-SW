package ap5

import ap5.TRNG

import spinal.sim._
import spinal.core._
import spinal.core.SpinalConfig
import spinal.core.sim.SimConfig
import spinal.lib._
import spinal.core.sim._

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

import scala.sys.process._

class TRNGTest extends AnyFlatSpec with Matchers {
  "A TRNG" should "do something" in {
    SimConfig.withConfig(SpinalConfig(targetDirectory = "rtl"))
                        .workspacePath("waves")
                        .withGhdl
                        .withWave
                        .compile { 
                        new TRNG(oscillators = 3)
              }.doSim(seed=0) { dut =>
                dut.clockDomain.forkStimulus(10)
                dut.clockDomain.waitSampling(1000)
                simSuccess()
              }
  }
}
