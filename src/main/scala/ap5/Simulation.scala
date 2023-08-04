package ap5
import java.awt
import java.awt.event.{ActionEvent, ActionListener, MouseEvent, MouseListener}
import java.io.{File, FileInputStream, FileOutputStream, IOException, OutputStream}
import com.sun.jna.Native;
import scopt.OptionParser
import java.sql.PseudoColumnUsage

import spinal.sim._
import spinal.core._
import spinal.core.sim._
import vexriscv.demo.{Murax, MuraxConfig}
import vexriscv.plugin._

import spinal.lib.com.jtag.sim.JtagTcp
import spinal.lib.com.uart.sim.{UartDecoder, UartEncoder}
import vexriscv.test.{JLedArray, JSwitchArray}

import scala.collection.mutable



case class SimulationConfig (
  initFile: String = null,
  withFst: Boolean = false,
  enableJtag: Boolean = false,
  enableMulAcc: Boolean = false,
  mulAccSize: Int = 4,
  mulAccSideChannel: Boolean = false, 
  ramSize: BigInt = 16 KiB
) {
  def config = {
   val config = MuraxConfig.default(withXip = false).copy(onChipRamSize = ramSize, onChipRamHexFile = initFile)
   if (enableMulAcc)
      config.cpuPlugins += new MulAccPlugin(size = mulAccSize,sideChannel = mulAccSideChannel, fifoDepth=mulAccSize)
   config.cpuPlugins(config.cpuPlugins.indexWhere(_.isInstanceOf[CsrPlugin])) = new CsrPlugin(CsrPluginConfig.all(0x80000000L))
   config.cpuPlugins(config.cpuPlugins.indexWhere(_.isInstanceOf[YamlPlugin])) = new YamlPlugin("simWorkspace/cpu0.yaml")
   config
  }
}

object Simulation {
  def main(args: Array[String]): Unit = {
    val optParser = new OptionParser[SimulationConfig]("Simulation") {
      head("Simulator")
      help("help").
        text("print usage text")
      opt[String]("initFile").
        action((s, c) => c.copy(initFile = s)).
        text("init file for RAM").
        valueName ("<hex>")
      opt[Int]("ramSize").
        action((i, c) => c.copy(ramSize = i KiB)).
        text("ram size for simulation in KiB").
        valueName ("<size>")
      opt[Unit]("jtag").
        action ((_, c) => c.copy(enableJtag = true))
      opt[Unit]("mulAcc").
        action ((_, c) => c.copy(enableMulAcc = true))
      opt[Unit]("mulAccSideChannel").
        action ((_, c) => c.copy(mulAccSideChannel = true))
      opt[Int]("mulAccSize").
        action((i, c) => c.copy(mulAccSize = i))
      opt[Unit]("fst").
        action((_,c) => c.copy(withFst=true))
    }
    val simConfig = optParser.parse(args, SimulationConfig()) match {
      case Some(simConfig) => simConfig
      case None         => ???
    }
    
    var sim = SimConfig.allOptimisation.workspacePath("simWorkspace/")
    if (simConfig.withFst) {
      sim = sim.withFstWave
    } 
    sim.compile(new Murax(simConfig.config)).doSim(seed=1917309779){dut =>
      if (simConfig.enableJtag && simConfig.initFile != null) {
        println("Warning: you provided both --initFile and --jtag, setting both together makes little sense")
      }

      if (!simConfig.enableJtag) {
        // Approx ~15 seconds of simulation time so that the trace doesn't get too large
        val timeout: Long = 705032704000L
        SimTimeout(timeout)
      }
      val mainClkPeriod = (1e12/dut.config.coreFrequency.toDouble).toLong
      val jtagClkPeriod = mainClkPeriod*4
      val uartBaudRate = 115200
      val uartBaudPeriod = (1e12/uartBaudRate).toLong

      val clockDomain = ClockDomain(dut.io.mainClk, dut.io.asyncReset)
      clockDomain.forkStimulus(mainClkPeriod)
      clockDomain.waitSampling(10)

      val tcpJtag = if(simConfig.enableJtag) JtagTcp(
        jtag = dut.io.jtag,
        jtagClkPeriod = jtagClkPeriod
      )

      val uartPin = dut.io.uart.txd
      var running = true
      val uartDecoder = fork {
      sleep(1)
      waitUntil(uartPin.toBoolean == true)
      try {
          while (running) {
            waitUntil(uartPin.toBoolean == false)
            sleep(uartBaudPeriod / 2)
            if (uartPin.toBoolean != false) {
              println("\rUART frame error (start bit)")
            } else {
              sleep(uartBaudPeriod)
              var byte = 0
              var i = 0
              while (i < 8) {
                if (uartPin.toBoolean) {
                  byte |= 1 << i
                }
                sleep(uartBaudPeriod)
                i += 1
              }
              if (uartPin.toBoolean) {
                // if(byte == 0xff)
                //   running = false
                System.out.write(byte)
              } else {
                println("\rUART frame error (stop bit)")
              }
            }
          }
        } catch {
          case io: IOException =>
        }
        println("\rUART decoder stopped")
      }

      if (!simConfig.enableJtag) {
        val exitHandler = fork {
          waitUntil(dut.io.gpioA.writeEnable.toLong % 2 == 1)
          /* Wait for the UART buffer to clear. TODO find better solution */
          sleep(mainClkPeriod*100000)
          if (dut.io.gpioA.write.toLong % 2 == 1) {
            simFailure();
          } else {
            simSuccess();
          }
        }
      }

      while (running) {
        sleep(mainClkPeriod*5000)
      }
      simSuccess();
    }
  }
}
