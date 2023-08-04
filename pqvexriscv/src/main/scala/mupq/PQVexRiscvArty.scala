package mupq

import java.io.{File, FileInputStream, FileOutputStream, IOException, OutputStream}

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
import spinal.lib.io.InOutWrapper

case class PipelinedMemoryBusXilinxRam(size: BigInt, latency: Int = 1) extends Component {
  require(size % 4 == 0, "Size must be multiple of 4 bytes")
  require(size > 0, "Size must be greater than zero")
  val busConfig = PipelinedMemoryBusConfig(log2Up(size), 32)
  val io = new Bundle {
    val bus = slave(PipelinedMemoryBus(busConfig))
  }

  val ram = new XilinxSinglePortRAM(
    dataWidth = 32,
    numWords = size / 4,
    readLatency = latency,
    byteWrite = true
  )

  ram.io.dina := io.bus.cmd.data
  ram.io.addra := io.bus.cmd.address.asBits >> 2
  ram.io.ena := io.bus.cmd.valid
  ram.io.wea := io.bus.cmd.write ? io.bus.cmd.mask | B"0000"
  ram.io.regcea := True
  ram.io.injectdbiterra := False
  ram.io.injectsbiterra := False
  ram.io.sleep := False
  io.bus.cmd.ready := True

  io.bus.rsp.valid := Delay(io.bus.cmd.fire && !io.bus.cmd.write, latency, init = False)
  io.bus.rsp.data := ram.io.douta
}

class PQVexRiscvArty(
  val ramBlockSizes: Seq[BigInt] = Seq[BigInt](64 KiB, 64 KiB),
  val clkFrequency: HertzNumber = 100 MHz,
  val coreFrequency: HertzNumber = 200 MHz,
  cpuPlugins: () => Seq[Plugin[VexRiscv]] = PQVexRiscv.withDSPMultiplier(),
  apbPeripherals: () => Seq[Peripheral[PQVexRiscv]] = PQVexRiscv.withUart(),
  ethernetClock: Boolean = true,
  ethernetReset: Boolean = true
)
extends PQVexRiscv(
  cpuPlugins = cpuPlugins,
  ibusRange = SizeMapping(0x80000000L, ramBlockSizes.reduce(_ + _)),
  apbPeripherals = apbPeripherals
) {
  val io = new Bundle {
    val RST = in Bool()
    val CLK = in Bool()

    val ETH_RSTN = ethernetReset.generate(out(Bool()))
    val ETH_REF_CLK = ethernetClock.generate(out(Bool()))
  }
  noIoPrefix()

  io.ETH_RSTN := True

  if (clkFrequency == coreFrequency && !ethernetClock) {
    asyncReset := !io.RST
    mainClock := io.CLK
  } else {
    val pll = new XilinxPLLBase(clkFrequency, Array(coreFrequency) ++ (if (ethernetClock) Array(25 MHz) else Array[HertzNumber]()))
    pll.io.CLKIN1 := io.CLK
    pll.io.RST := !io.RST
    pll.io.CLKFBIN := pll.io.CLKFBOUT
    pll.io.PWRDWN := False

    val bufg = new XilinxGlobalBuffer()
    bufg.io.I := pll.io.CLKOUT0

    asyncReset := !io.RST && !pll.io.LOCKED
    mainClock := bufg.io.O

    if (ethernetClock) {
      io.ETH_REF_CLK := pll.io.CLKOUT1
    }
  }

  // io.TDO := jtag.tdo
  // jtag.tck := io.TCK
  // jtag.tdi := io.TDI
  // jtag.tms := io.TMS

  // uart.rxd := io.RXD
  // io.TXD := uart.txd

  val memory = new ClockingArea(systemClockDomain) {
    val ramBlocks       = ramBlockSizes.zipWithIndex.map(t => PipelinedMemoryBusXilinxRam(t._1, 2))
    var curAddr: BigInt = 0x80000000L
    for (block <- ramBlocks) {
      busSlaves += block.io.bus -> SizeMapping(curAddr, block.size)
      curAddr += block.size
    }
  }
}

object PQVexRiscvArty {
  def main(args: Array[String]): Unit = {
    case class PQVexRiscvArtyConfig(
      ramBlocks: Seq[BigInt] = Seq(64 KiB, 64 KiB),
      clkFrequency: HertzNumber = 100 MHz,
      coreFrequency: HertzNumber = 300 MHz,
      cpuPlugins: () => Seq[Plugin[VexRiscv]] = PQVexRiscv.baseConfig(),
      apbPeripherals: () => Seq[Peripheral[PQVexRiscv]] = PQVexRiscv.withUart(),
      ethernet: Boolean = false
    )
    val optParser = new OptionParser[PQVexRiscvArtyConfig]("PQVexRiscvArty") {
      head("PQVexRiscvArty board")
      help("help") text ("print usage text")
      opt[Seq[Int]]("ram") action ((r, c) =>
        c.copy(ramBlocks =
          r.map(_ KiB))) text ("SRAM Blocks in KiB") valueName ("<block1>,<block2>")
      opt[Int]("clk") action ((r, c) =>
        c.copy(clkFrequency = (r MHz))) text ("Input clock freqency in MHz") valueName ("<freq>")
      opt[Int]("core") action ((r, c) =>
        c.copy(coreFrequency = (r MHz))) text ("Target core freqency in MHz") valueName ("<freq>")
      opt[Unit]("mul") action ((_, c) =>
        c.copy(cpuPlugins = PQVexRiscv.withDSPMultiplier(c.cpuPlugins)))
      opt[Unit]("timer") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withTimer(base = c.apbPeripherals)))
      opt[Unit]("ethernet") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withMac(base = c.apbPeripherals), ethernet = true))
      opt[Unit]("mdio") action ((_, c) =>
        c.copy(apbPeripherals = PQVexRiscv.withMdio(base = c.apbPeripherals)))
    }
    val config = optParser.parse(args, PQVexRiscvArtyConfig()) match {
      case Some(config) => config
      case None         => ???
    }
    val report = SpinalConfig(
      mode = Verilog,
      targetDirectory = "rtl/gen"
    ).generate(
      InOutWrapper(
        new PQVexRiscvArty(
          ramBlockSizes = config.ramBlocks,
          clkFrequency = config.clkFrequency,
          coreFrequency = config.coreFrequency,
          cpuPlugins = config.cpuPlugins,
          apbPeripherals = config.apbPeripherals,
          ethernetReset = config.ethernet,
          ethernetClock = config.ethernet
        )
      )
    )
    println(s"Core freqency is set to ${config.coreFrequency.toDouble / 1e6} MHz")
    report.mergeRTLSource(s"rtl/gen/${report.toplevelName}.aux")
  }
}
