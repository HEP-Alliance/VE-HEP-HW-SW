package mupq

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

case class PipelinedMemoryBusRam(size: BigInt, initialContent: File = null) extends Component {
  require(size % 4 == 0, "Size must be multiple of 4 bytes")
  require(size > 0, "Size must be greater than zero")
  val busConfig = PipelinedMemoryBusConfig(log2Up(size), 32)
  val io = new Bundle {
    val bus = slave(PipelinedMemoryBus(busConfig))
  }

  val ram = Mem(Bits(32 bits), size / 4)
  io.bus.rsp.valid := Delay(io.bus.cmd.fire && !io.bus.cmd.write, 2, init = False)
  val rdata = ram.readWriteSync(
    address = io.bus.cmd.address >> 2,
    data = io.bus.cmd.data,
    enable = io.bus.cmd.valid,
    write = io.bus.cmd.write,
    mask = io.bus.cmd.mask
  )
  io.bus.rsp.data := RegNext(rdata) init (0)
  io.bus.cmd.ready := True

  if (initialContent != null) {
    val input       = new FileInputStream(initialContent)
    val initContent = Array.fill[BigInt](ram.wordCount)(0)
    val fileContent = Array.ofDim[Byte](Seq(input.available, initContent.length * 4).min)
    input.read(fileContent)
    for ((byte, addr) <- fileContent.zipWithIndex) {
      val l = java.lang.Byte.toUnsignedLong(byte) << ((addr & 3) * 8)
      initContent(addr >> 2) |= BigInt(l)
    }
    ram.initBigInt(initContent)
  }
}

class PQVexRiscvSim(
  val ramBlockSizes: Seq[BigInt] = Seq[BigInt](256 KiB, 128 KiB),
  val initialContent: File = null,
  val coreFrequency: HertzNumber = 12 MHz,
  cpuPlugins: () => Seq[Plugin[VexRiscv]] = PQVexRiscv.withDSPMultiplier(),
  apbPeripherals: () => Seq[Peripheral[PQVexRiscv]] = PQVexRiscv.withUart()
)
extends PQVexRiscv(
  cpuPlugins = cpuPlugins,
  ibusRange = SizeMapping(0x80000000L, ramBlockSizes.reduce(_ + _)),
  apbPeripherals
) {
  val io = new Bundle {
    val reset = in Bool ()
    val clk   = in Bool ()
  }

  asyncReset := io.reset
  mainClock := io.clk

  val memory = new ClockingArea(systemClockDomain) {
    val ramBlocks =
      ramBlockSizes.zipWithIndex.map(t =>
        PipelinedMemoryBusRam(t._1, if (t._2 == 0) initialContent else null))
    var curAddr: BigInt = 0x80000000L
    for (block <- ramBlocks) {
      busSlaves += block.io.bus -> SizeMapping(curAddr, block.size)
      curAddr += block.size
    }
  }
}

object PQVexRiscvSim {

  def main(args: Array[String]) = {
    case class PQVexRiscvSimConfig(
      uartOutFile: OutputStream = System.out,
      enablePts: Boolean = false,
      initFile: File = null,
      ramBlocks: Seq[BigInt] = Seq(256 KiB, 128 KiB),
      cpuPlugins: () => Seq[Plugin[VexRiscv]] = PQVexRiscv.withDSPMultiplier(),
      apbPeripherals: () => Seq[Peripheral[PQVexRiscv]] = PQVexRiscv.withUart(),
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
      opt[Unit]("mdio") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withMdio(base = c.apbPeripherals)))
      opt[Unit]("timer") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withTimer(base = c.apbPeripherals)))
      opt[Unit]("wave") action ((_, c) =>
        c.copy(wave = true))
    }

    val config = optParser.parse(args, PQVexRiscvSimConfig()) match {
      case Some(config) => config
      case None         => ???
    }

    val simConfig = SimConfig.allOptimisation
    if (config.wave)
      simConfig.withFstWave

    val compiled = simConfig.compile {
      new PQVexRiscvSim(
        config.ramBlocks,
        config.initFile,
        cpuPlugins = config.cpuPlugins,
        apbPeripherals = config.apbPeripherals
      )
    }

    var pts = if (config.enablePts) new PseudoTerminal() else null

    if (pts != null) {
      println(f"Opened pts under ${pts.getPtsName}, UART output will be directed there.")
    }

    compiled.doSim("PqVexRiscvSim", 42) { dut =>
      val mainClkPeriod  = (1e12 / dut.coreFrequency.toDouble).toLong
      val jtagClkPeriod  = mainClkPeriod * 4
      val uartBaudRate   = 115200
      val uartBaudPeriod = (1e12 / uartBaudRate.toDouble).toLong

      val clockDomain = ClockDomain(dut.io.clk, dut.io.reset)
      clockDomain.forkStimulus(mainClkPeriod)

      val tcpJtag = JtagTcp(
        jtag = dut.jtag,
        jtagClkPeriod = jtagClkPeriod
      )

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

      dut.peripheral(classOf[MDIOPeripheral]) match {
        case None =>
        case Some(mdioPeripheral) =>
          val mdc = mdioPeripheral.mdio.C
          val mdio = mdioPeripheral.mdio.IO
          mdio.read #= true
          waitUntil(!mdc.toBoolean)

          def sampleBit(send : Boolean = true) : Int = {
            mdio.read #= send
            waitUntil(mdc.toBoolean)
            /* Sample the bit */
            val result : Int = if (mdio.writeEnable.toBoolean) (if (mdio.write.toBoolean) 0x1 else 0x0) else (if (send) 0x1 else 0x0)
            /* Wait for negEdge */
            waitUntil(!mdc.toBoolean)
            result
          }

          val mdioEncoder = fork {
            var synced = 0
            var sending = false

            def recvBits(n : Int, send : Int = -1) : Int = {
              var result = 0
              for (i <- (n-1) downto 0) {
                result = (result << 1) | sampleBit(((send >> i) & 0x1) == 1)
              }
              result
            }

            while (true) {
              synced = 0
              while (synced < 32) {
                synced = if (sampleBit().equals(1)) synced + 1 else 0
              }
              if (!recvBits(2).equals(1)) {
                println("Weird MDIO Start")
              }
              val opcode = recvBits(2)
              val phy = recvBits(5)
              val reg = recvBits(5)
              if (!recvBits(2).equals(2)) {
                println("Weird MDIO turnaround")
              }
              val data = recvBits(16, 0xDEAD)
              println(f"MDIO OpCode ${opcode} Phy ${phy}%02x Reg ${reg}%02x Data ${reg}%02x")
            }
          }
      }

      var running = true

      while (running) {
        sleep(mainClkPeriod * 50000)
      }
    }
  }

}
