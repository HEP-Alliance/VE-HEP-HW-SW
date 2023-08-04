package ap5

import vexriscv.plugin.Mul16Plugin
import vexriscv.plugin.MulDivIterativePlugin

import java.io.{File, FileInputStream, FileOutputStream, IOException, OutputStream}

import com.sun.jna.Native;

import scopt.OptionParser

import spinal.sim._
import spinal.core._
import spinal.lib._
import spinal.core.sim._

import spinal.lib.bus.simple._
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.io.TriStateArray
import spinal.lib.com.jtag.Jtag
import spinal.lib.com.uart.Uart
import spinal.lib.com.jtag.sim.JtagTcp

import vexriscv.VexRiscv
import vexriscv.plugin.Plugin
import java.sql.PseudoColumnUsage
import java.util.Arrays
import scala.collection.mutable.ArrayBuffer



import mupq._

object HSMSim {

  def main(args: Array[String]) = {
    case class PQVexRiscvSimConfig(
      uartOutFile: OutputStream = System.out,
      enablePts: Boolean = false,
      initFile: File = null,
      ramBlocks: Seq[BigInt] = Seq(256 KiB, 128 KiB),
      cpuPlugins: () => Seq[Plugin[VexRiscv]] = PQVexRiscvVEHEP.configBase(),
      apbPeripherals: () => Seq[Peripheral[PQVexRiscv]] = PQVexRiscv.withUart(),
      debugInterrupts: Boolean = false,
      wave: Boolean = false
    )
    val optParser = new OptionParser[PQVexRiscvSimConfig]("PQVexRiscvSim") {
      head("PQVexRiscvSim simulator")
      help("help") text ("print usage text")
      opt[File]("uart") action ((f, c) =>
        c.copy(uartOutFile =
          new FileOutputStream(
            f,
            true))) text ("File for UART output (will be appended)") valueName ("<output>")
      opt[Unit]("pts") action ((_, c) => c.copy(enablePts = true))
      opt[File]("init") action ((f, c) =>
        c.copy(initFile = f)) text ("Initialization file for first RAM block") valueName ("<bin>")
      opt[Seq[Int]]("ram") action ((r, c) =>
        c.copy(ramBlocks =
          r.map(_ KiB))) text ("SRAM Blocks in KiB") valueName ("<block1>,<block2>")
      opt[Unit]("mulacc") action ((_, c) =>                                
        c.copy(cpuPlugins = () => c.cpuPlugins() ++ Seq(new CombaPlugin, new MulDivIterativePlugin(genMul=false, genDiv=true))))
      opt[Unit]("mdio") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withMdio(base = c.apbPeripherals)))
      opt[Unit]("timer") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withTimer(base = c.apbPeripherals)))
      opt[Unit]("gpio") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withGpio(gpioWidth = 32, base = c.apbPeripherals)))
      opt[Unit]("spi") action ((_, c) =>
        c.copy(apbPeripherals = SpiPeripherals.withSpiSlave(base = c.apbPeripherals)))
      opt[Unit]("aes") action ((_, c) =>
        c.copy(apbPeripherals = AESPeripherals.withAES(base = c.apbPeripherals)))
      opt[Unit]("aes-masked") action ((_, c) =>
        c.copy(apbPeripherals = AESMaskedPeripherals.withAESMasked(base = c.apbPeripherals)))
      opt[Unit]("wave") action ((_, c) =>
        c.copy(wave = true))
      opt[Unit]("debug-interrupts") action ((_, c) =>
        c.copy(debugInterrupts = true))
    }

    val config = optParser.parse(args, PQVexRiscvSimConfig()) match {
      case Some(config) => config
      case None         => ???
    }

    var simConfig = SimConfig.allOptimisation.addSimulatorFlag("-Wno-TIMESCALEMOD")
    if (config.wave) {
      simConfig = simConfig.withFstWave
    }


    val compiled = simConfig.compile {
      val dut = new PQVexRiscvSim(
        config.ramBlocks,
        config.initFile,
        cpuPlugins = config.cpuPlugins,
        apbPeripherals = config.apbPeripherals
      )
      //if (config.debugInterrupts) {
      //  dut.core.externalInterrupt.simPublic
      //  dut.core.timerInterrupt.simPublic
      //  dut.core.softwareInterrupt.simPublic
      //}
      //val dut = new PQVexRiscvVEHEP(
      //  cpuPlugins = config.cpuPlugins,
      //  apbPeripherals = config.apbPeripherals,
      //  ecp5 = false
      //)
      dut
    }

    var pts = if (config.enablePts) new PseudoTerminal() else null

    if (pts != null) {
      println(f"Opened pts under ${pts.getPtsName}, UART output will be directed there.")
    }

    compiled.doSim("PqVexRiscvSim", 42) { dut =>
      val mainClkPeriod  = (1e12 / dut.coreFrequency.toDouble).toLong
      val jtagClkPeriod  = mainClkPeriod * 4
      val spiClkPeriod  = mainClkPeriod * 1000
      val uartBaudRate   = 115200
      val uartBaudPeriod = (1e12 / uartBaudRate.toDouble).toLong

      val clockDomain = ClockDomain(dut.io.clk)
      clockDomain.forkStimulus(mainClkPeriod)
      dut.io.reset #= false

      val tcpJtag = JtagTcp(
        jtag = dut.jtag,
        jtagClkPeriod = jtagClkPeriod
      )

      dut.peripheral(classOf[SpiSlavePeripheral]) match {
        case None =>
        case Some(spiPeripheral) => 

        val tcpSpi = SpiTcp(
          spi = spiPeripheral.spi,
          spiClkPeriod
        )
      }
  //    if (config.wave) {
  //      val timeout: Long = 7050327040L*10
  //      SimTimeout(timeout)
  //    }

      println(s"Simulating ${dut.getClass.getName} with JtagTcp on port 7894")

      dut.peripheral(classOf[UARTPeripheral]) match {
        case None =>
        case Some(uartPeripheral) =>
          val uartTxd = uartPeripheral.uart.txd
          val uartRxd = uartPeripheral.uart.rxd
          uartRxd #= true

          val uartDecoder = fork {
            sleep(1)
            waitUntil(uartTxd.toBoolean == true)
            try {
              while (true) {
                waitUntil(uartTxd.toBoolean == false)
                sleep(uartBaudPeriod / 2)
                if (uartTxd.toBoolean != false) {
                  println("\rUART frame error (start bit)")
                } else {
                  sleep(uartBaudPeriod)
                  var byte = 0
                  var i    = 0
                  while (i < 8) {
                    if (uartTxd.toBoolean) {
                      byte |= 1 << i
                    }
                    sleep(uartBaudPeriod)
                    i += 1
                  }
                  if (uartTxd.toBoolean) {
                    if (pts != null) {
                      pts.writeByte(byte)
                    } else {
                      config.uartOutFile.write(byte)
                    }
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

          val uartEncoder = if (pts != null) fork {
            uartRxd #= true
            while (true) {
              val b = pts.readByte()
              if (b != -1) {
                uartRxd #= false
                sleep(uartBaudPeriod)

                (0 to 7).foreach { bitId =>
                  uartRxd #= ((b >> bitId) & 1) != 0
                  sleep(uartBaudPeriod)
                }

                uartRxd #= true
                sleep(uartBaudPeriod)
              } else {
                sleep(uartBaudPeriod * 1000)
              }
            }
          }
      }
//*
      dut.peripheral(classOf[GPIOPeripheral]) match {
        case None =>
        case Some(gpioPeripheral) =>
          val gpio =  gpioPeripheral.gpio
          sleep(mainClkPeriod*50000)
          val exitHandler = fork {
             waitUntil(gpio.writeEnable.toLong % 2 == 1)
             sleep(mainClkPeriod*50000)
             if (gpio.write.toLong % 2 == 1) {
               simFailure();
             } else {
               simSuccess();
             }
           }
      }//*/


      var running = true
/*
      ///[Error]
      ///home/waelde/work/formal-verification-examples/src/main/scala/ap5/HSMSim.scala:196:
      //value interrupt is not a member of spinal.core.ClockingArea{val
      //timerInterrupt: spinal.core.Bool; val externalInterrupt:
      //spinal.core.Bool; val softwareInterrupt: spinal.core.Bool; val config:
      //vexriscv.VexRiscvConfig; val cpu: vexriscv.VexRiscv; def ibus:
      //spinal.lib.bus.simple.PipelinedMemoryBus; def ibus_=(x$1:
      //spinal.lib.bus.simple.PipelinedMemoryBus): Unit; def dbus:
      //spinal.lib.bus.simple.PipelinedMemoryBus; def dbus_=(x$1:
      //spinal.lib.bus.simple.PipelinedMemoryBus): Unit}
      val interruptDebuger = 
        if (config.debugInterrupts) 
          fork {
            while(running)  {
              val externalInterrupt = dut.core.externalInterrupt.toBoolean
              val timerInterrupt = dut.core.timerInterrupt.toBoolean
              val softwareInterrupt = dut.core.softwareInterrupt.toBoolean

              if (externalInterrupt) {
                println("[!] externalInterrupt")
              }
              if (timerInterrupt) {
                println("[!] timerInterrupt")
              }
              if (softwareInterrupt) {
                println("[!] softwareInterrupt")
              } 
              sleep(mainClkPeriod)
            }
          }
*/
      while (running) {
        sleep(mainClkPeriod * 500)
      }
    }
  }

}




class TRNGPeripheral(val addrOffset: BigInt = 0x50000) extends Peripheral[PQVexRiscv]{
  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {
      val source = Apb3EntropySource(Apb3EntropySource.getApb3Config)
      soc.apbSlaves += source.io.bus -> (addrOffset, 64 KiB)
    }
  }
}

object HSMVEHEP {
  def cpuplugins() =  
     mupq.PQVexRiscv.baseConfig(base = () => Seq(new CombaPlugin))

  def peripherals() = 
    mupq.PQVexRiscv.withUart(base = () => Seq())

  def main(args: Array[String]): Unit = {

  val report = SpinalConfig(
    mode = Verilog,
    targetDirectory = "./out/"
    ).generate (
      new PQVexRiscvVEHEP(cpuPlugins=cpuplugins(),apbPeripherals=peripherals(),ecp5=false)
     )
    report.mergeRTLSource(s"out/${report.toplevelName}.aux")
  }
}
