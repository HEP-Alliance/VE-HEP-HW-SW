package mupq

import scopt.OptionParser

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._

import vexriscv._
import vexriscv.plugin._

class PipelinedMemoryBusSPRAM(busConfig: PipelinedMemoryBusConfig) extends Component {
  require(busConfig.dataWidth == 32, "Only 32 bit busses")
  val io = new Bundle {
    val bus = slave(PipelinedMemoryBus(busConfig))
  }

  /* Tie together two RAMS to get 32-bit width */
  val rams: Array[Ice40SPRAM] = (0 to 1).map(_ => new Ice40SPRAM).toArray
  val enable                  = io.bus.cmd.valid
  val mask                    = io.bus.cmd.mask
  /* Fan out the simple byte mask of the bus to bit masks */
  val maskLow  = mask(1) ## mask(1) ## mask(0) ## mask(0)
  val maskHigh = mask(3) ## mask(3) ## mask(2) ## mask(2)
  for ((ram, i) <- rams.zipWithIndex) {
    /* Don't ever sleep */
    ram.io.POWEROFF := True
    ram.io.STANDBY := False
    ram.io.SLEEP := False
    // Bus assignments
    ram.io.CHIPSELECT := enable
    ram.io.ADDRESS := io.bus.cmd.address(15 downto 2).asBits
    ram.io.WREN := io.bus.cmd.write
    if (i % 2 == 0) {
      ram.io.MASKWREN := maskLow
      ram.io.DATAIN := io.bus.cmd.data(15 downto 0)
    } else {
      ram.io.MASKWREN := maskHigh
      ram.io.DATAIN := io.bus.cmd.data(31 downto 16)
    }
  }
  /* Always ready */
  io.bus.cmd.ready := True

  /* Memory is synchronous, so response is ready one cycle later */
  io.bus.rsp.valid := Delay(io.bus.cmd.fire && !io.bus.cmd.write, 2, init = False)
  io.bus.rsp.data := Delay(rams(1).io.DATAOUT ## rams(0).io.DATAOUT, 1, init = B(0))
}

class PQVexRiscvUP5K(
  val coreFrequency: HertzNumber = 12 MHz,
  cpuPlugins: () => Seq[Plugin[VexRiscv]] = PQVexRiscv.withDSPMultiplier(),
  apbPeripherals: () => Seq[Peripheral[PQVexRiscv]] = PQVexRiscv.withUart()
)
extends PQVexRiscv(
  cpuPlugins = cpuPlugins,
  ibusRange = SizeMapping(0x80000000L, 128 KiB),
  apbPeripherals = apbPeripherals
) {
  val io = new Bundle {
    val ice_clk = in Bool()
    // /* UART */
    // val iob_8a = out Bool() // TXD
    // val iob_9b = in Bool()  // RXD
    // /* JTAG */
    // val iob_23b    = out Bool() // TDO
    // val iob_25b_g3 = in Bool()  // TCK
    // val iob_24a    = in Bool()  // TDI
    // val iob_29b    = in Bool()  // TMS
  }
  asyncReset := False
  mainClock := io.ice_clk
  /* Remove io_ prefix from generated Verilog */
  noIoPrefix()
  /* PLL */
  // val pll = new Ice40PLLPad(
  //   divR = 0,
  //   divF = 52,
  //   divQ = 5)

  //  pll.io.PACKAGEPIN := io.ice_clk
  //  pll.io.BYPASS := False
  //  pll.io.RESETB := True
  /* Plugins */
  // io.iob_23b := jtag.tdo
  // jtag.tck := io.iob_25b_g3
  // jtag.tdi := io.iob_24a
  // jtag.tms := io.iob_29b

  // uart.rxd := io.iob_9b
  // io.iob_8a := uart.txd

  val memory = new ClockingArea(systemClockDomain) {
    val ram1 = new PipelinedMemoryBusSPRAM(busConfig)
    busSlaves += ram1.io.bus -> SizeMapping(0x80000000L, 64 KiB)
    val ram2 = new PipelinedMemoryBusSPRAM(busConfig)
    busSlaves += ram2.io.bus -> SizeMapping(0x80000000L + (64 KiB).toLong, 64 KiB)
  }
}

object PQVexRiscvUP5K {
  def main(args: Array[String]): Unit = {
    case class PQVexRiscvUP5KConfig(
      cpuPlugins: () => Seq[Plugin[VexRiscv]] = PQVexRiscv.baseConfig()
    )
    val optParser = new OptionParser[PQVexRiscvUP5KConfig]("PQVexRiscvUP5K") {
      head("PQVexRiscvUP5K board")
      help("help") text ("print usage text")
      opt[Unit]("mul") action ((_, c) =>
        c.copy(cpuPlugins = PQVexRiscv.withDSPMultiplier(c.cpuPlugins)))
//      opt[Unit]("trng") action ((_, c) =>
//        c.copy(apbPeripherals = PQVexRiscv.withTRNG(base = c.apbPeripherals)))
    }
    val config = optParser.parse(args, PQVexRiscvUP5KConfig()) match {
      case Some(config) => config
      case None         => ???
    }
    val report = SpinalConfig(
      mode = Verilog,
      targetDirectory = "rtl/gen"
    ).generate(
      new PQVexRiscvUP5K(
        cpuPlugins = config.cpuPlugins
      )
    )
    report.mergeRTLSource(s"rtl/gen/${report.toplevelName}.aux")
  }
}
