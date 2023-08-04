package ap5

import spinal.sim._
import spinal.core._
import spinal.core.SpinalConfig
import spinal.core.sim.SimConfig
import spinal.lib._
import spinal.core.sim._

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

import scala.sys.process._
import scala.util.Random
import scala.BigInt

import ap5.MulAcc
import ap5.Command

class MulAccTester extends AnyFlatSpec with Matchers {
  val accSize = 4
  def sendStreamCommand(clk: ClockDomain, cmd: Stream[Command], out: Stream[UInt], op: Int, data: BigInt, index: Int) = {
    var ret = BigInt(0)
    cmd.data #= data
    cmd.op #= op
    cmd.index #= index
    clk.onNextSampling {
      cmd.valid #= true
    }
    do {
      clk.waitSampling
      if(op == 2 && out.valid.toBoolean) {
        ret = out.payload.toBigInt
        out.ready #= true
      }
    }
    while (cmd.ready.toBoolean == false)
    cmd.valid #= false
    if (op == 2) {
        out.ready #= false
    }
    ret
  }
  def sendReset(clk: ClockDomain, cmd: Stream[Command]) = {
      sendStreamCommand(clk,cmd,null,0,0,0)
  }
  def sendAdd(clk: ClockDomain, cmd: Stream[Command],data: BigInt, index: Int) = {
      sendStreamCommand(clk,cmd,null,1,data,index)
  }
  def readNum(clk: ClockDomain, cmd: Stream[Command], out: Stream[UInt], index: Int) = {
      sendStreamCommand(clk,cmd,out,2,0,index)
  }
  def readNums(clk: ClockDomain, cmd: Stream[Command], out: Stream[UInt]) = {
    (0 until 3).map {x => readNum(clk,cmd,out,x)}.reduce {(x,y) => (x<<32)+y}
  }
  var compiled: SimCompiled[MulAcc] = null
  "A MulAcc" should "compile" in {
    compiled = SimConfig.withConfig(SpinalConfig(targetDirectory = "rtl"))
                        .workspacePath("waves")
                        .withWave
                        .compile { 
                        //new MulAcc(size=4)
                        var dut = new MulAcc(size=accSize)
                        dut.io.input.payload.simPublic()
                        dut.io.input.valid.simPublic
                        dut.io.input.ready.simPublic
                        dut
              }
  }
  it should "reset properly" in {
    compiled.doSim(seed=0) { dut =>
      dut.clockDomain.forkStimulus(10)
      dut.clockDomain.waitSampling(10)
      sendReset(dut.clockDomain,dut.io.input)
      assert(readNums(dut.clockDomain,dut.io.input,dut.io.output) === 0)
      simSuccess()
    }
  }
  it should "do one addition correctly" in {
    val index = Random.nextInt(accSize - 2)
    var num = BigInt(32, Random) // one random 64bit integer
    println(num)
    compiled.doSim(seed=0) { dut =>
      dut.clockDomain.forkStimulus(10)
      dut.clockDomain.waitSampling(10)
      // reset the accumulator
      sendReset(dut.clockDomain,dut.io.input)
      sendReset(dut.clockDomain,dut.io.input)
      sendAdd(dut.clockDomain,dut.io.input,num,index)
      assert((num<<(index*32)) === readNums(dut.clockDomain,dut.io.input,dut.io.output))
      simSuccess()
    }
  }
  }
