package mupq

import scala.collection.mutable.ArrayBuffer

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._
import spinal.lib.com.jtag._

import vexriscv._
import vexriscv.plugin._

abstract class PQVexRiscv(
  cpuPlugins: () => Seq[Plugin[VexRiscv]],
  ibusRange: SizeMapping,
  apbPeripherals: () => Seq[Peripheral[PQVexRiscv]]
)
extends Component {
  val coreFrequency: HertzNumber

  /* Clock and resets */

  val asyncReset: Bool = Bool

  val mainClock: Bool = Bool

  val resetCtrlClockDomain: ClockDomain =
    ClockDomain(clock = mainClock, config = ClockDomainConfig(resetKind = BOOT))

  val resetCtrl = new ClockingArea(resetCtrlClockDomain) {
    val bufferedReset = BufferCC(asyncReset)

    val mainClockReset   = RegNext(bufferedReset)
    val systemClockReset = RegNext(bufferedReset)
  }

  val systemClockDomain: ClockDomain = ClockDomain(
    clock = mainClock,
    reset = resetCtrl.systemClockReset,
    frequency = FixedFrequency(coreFrequency)
  )

  val debugClockDomain: ClockDomain = ClockDomain(
    clock = mainClock,
    reset = resetCtrl.mainClockReset,
    frequency = FixedFrequency(coreFrequency)
  )

  /* Bus interconnect */
  val busConfig = PipelinedMemoryBusConfig(
    addressWidth = 32,
    dataWidth = 32
  )

  val busSlaves  = ArrayBuffer[(PipelinedMemoryBus, SizeMapping)]()
  val busMasters = ArrayBuffer[(PipelinedMemoryBus, SizeMapping)]()

  /* VexRiscv Core */
  var jtag: Jtag = null

  val core = new ClockingArea(systemClockDomain) {
    val timerInterrupt    = Bool()
    val externalInterrupt = Bool()
    val softwareInterrupt = Bool()
    timerInterrupt := False
    externalInterrupt := False
    softwareInterrupt := False

    val config = VexRiscvConfig(plugins = cpuPlugins() ++ Seq(new DebugPlugin(debugClockDomain, 3)))

    val cpu = new VexRiscv(config)
    /* Wire the Busses / Lines to the plugins */
    var ibus: PipelinedMemoryBus = PipelinedMemoryBus(busConfig)
    var dbus: PipelinedMemoryBus = PipelinedMemoryBus(busConfig)
    for (plugin <- cpu.plugins) plugin match {
      case plugin: IBusSimplePlugin =>
        val cpuibus = plugin.iBus.toPipelinedMemoryBus()
        ibus.cmd <-/< cpuibus.cmd
        ibus.rsp >> cpuibus.rsp
      case plugin: DBusSimplePlugin =>
        val cpudbus = plugin.dBus.toPipelinedMemoryBus()
        dbus.cmd <-/< cpudbus.cmd
        dbus.rsp >> cpudbus.rsp
        plugin.dBus.rsp.error := False
      case plugin: CsrPlugin =>
        plugin.externalInterrupt := externalInterrupt
        plugin.timerInterrupt := timerInterrupt
        plugin.softwareInterrupt := softwareInterrupt
      case plugin: DebugPlugin =>
        plugin.debugClockDomain {
          resetCtrl.systemClockReset setWhen (RegNext(plugin.io.resetOut))
          jtag = slave(Jtag()) setName("jtag")
          jtag <> plugin.io.bus.fromJtag()
        }
      case _ =>
    }

    busMasters += dbus -> SizeMapping(0L, (1L << 32L))
    busMasters += ibus -> ibusRange
  }

  /* Peripherals */
  val apbSlaves  = ArrayBuffer[(Apb3, SizeMapping)]()
  val peripherals = apbPeripherals()
  peripherals.foreach(_.setup(this))
  peripherals.foreach(_.build(this))

  def buildInterconnect(): Unit = {
    assert(!SizeMapping.verifyOverlapping(apbSlaves.map(_._2)))
    val apbCrossbar = (apbSlaves.length > 0).generate {
      new ClockingArea(systemClockDomain) {
        val apbBridge = new PipelinedMemoryBusToApbBridge(
          apb3Config = Apb3Config(
            addressWidth = 20,
            dataWidth = 32
          ),
          pipelineBridge = true,
          pipelinedMemoryBusConfig = busConfig
        )

        busSlaves += apbBridge.io.pipelinedMemoryBus -> SizeMapping(0xf0000000L, 1 MiB)

        val apbDecoder = Apb3Decoder(
          master = apbBridge.io.apb,
          slaves = apbSlaves
        )
      } setName("apbCrossbar")
    }
    assert(!SizeMapping.verifyOverlapping(busSlaves.map(_._2)))
    val crossbar = new ClockingArea(systemClockDomain) {
      val interconnect = new PipelinedMemoryBusInterconnect()
      interconnect.perfConfig()
      /* Setup the interconnect */
      interconnect.addSlaves(busSlaves: _*)
      /* Check which masters overlap with which slaves */
      def overlaps(a: SizeMapping, b: SizeMapping): Boolean =
        if (a.base < b.base) a.end >= b.base else b.end >= a.base
      interconnect.addMasters(
        busMasters.map(m =>
          m._1 -> busSlaves.filter(s => overlaps(m._2, s._2)).map(s => s._1).toSeq): _*
      )
    } setName("crossbar")
  }

  def peripheral[T <: Peripheral[PQVexRiscv]](clazz : Class[T]) : Option[T] = {
    val filtered = peripherals.filter(o => clazz.isAssignableFrom(o.getClass))
    if (filtered.length < 1)
      None
    else
      Some(filtered.head.asInstanceOf[T])
  }

  Component.current.addPrePopTask(() => buildInterconnect())
}

object PQVexRiscv {
  type PluginSeq = Seq[Plugin[VexRiscv]]
  type PluginGen = () => PluginSeq

  /** Basic set of Plugins (conforms mostly to rv32i) */
  def baseConfig(base: PluginGen = () => Seq()) = () =>
    base() ++ Seq(
      new IBusSimplePlugin(
        resetVector = 0x80000000L,
        cmdForkOnSecondStage = true,
        cmdForkPersistence = false,
        prediction = NONE,
        catchAccessFault = false,
        compressedGen = false
      ),
      new DBusSimplePlugin(
        catchAddressMisaligned = false,
        catchAccessFault = false,
        earlyInjection = false
      ),
      new CsrPlugin(
        CsrPluginConfig
          .smallest(0x80000000L)
          .copy(
            mtvecAccess = CsrAccess.READ_WRITE,
            mcycleAccess = CsrAccess.READ_ONLY,
            minstretAccess = CsrAccess.READ_ONLY
          )
      ),
      new DecoderSimplePlugin(
        catchIllegalInstruction = false
      ),
      new RegFilePlugin(
        regFileReadyKind = plugin.SYNC,
        zeroBoot = false
      ),
      new IntAluPlugin,
      new SrcPlugin(
        separatedAddSub = false,
        executeInsertion = false
      ),
      new FullBarrelShifterPlugin,
      new HazardSimplePlugin(
        bypassExecute = true,
        bypassMemory = true,
        bypassWriteBack = true,
        bypassWriteBackBuffer = true,
        pessimisticUseSrc = false,
        pessimisticWriteRegFile = false,
        pessimisticAddressMatch = false
      ),
      new BranchPlugin(
        earlyBranch = false,
        catchAddressMisaligned = false
      ),
      new YamlPlugin("cpu0.yaml")
    )

  /** Plugins for a small multiplier */
  def smallMultiplier = Seq(
    new MulDivIterativePlugin(
      genMul = true,
      genDiv = true,
      mulUnrollFactor = 1,
      divUnrollFactor = 1
    )
  )

  /** Config with a small multiplier */
  def withSmallMultiplier(base: PluginGen = baseConfig()) = () => base() ++ smallMultiplier

  /** Plugins for a multiplier for FPGAs */
  def dspMultiplier = Seq(
    new Mul16Plugin,
    new MulDivIterativePlugin(
      genMul = false,
      genDiv = true,
      divUnrollFactor = 1
    )
  )

  /** Config with a multiplier for FPGAs */
  def withDSPMultiplier(base: PluginGen = baseConfig()) = () => base() ++ dspMultiplier

  type PeriphSeq = Seq[Peripheral[PQVexRiscv]]
  type PeriphGen = () => PeriphSeq

  /** GPIO */
  def withGpio(gpioWidth: Int, addrOffset: BigInt = 0x0000, base: PeriphGen = () => Seq()) = () => base() ++ Seq(new GPIOPeripheral(gpioWidth, addrOffset))

  /** UART */
  def withUart(addrOffset: BigInt = 0x10000, base: PeriphGen = () => Seq()) = () => base() ++ Seq(new UARTPeripheral(addrOffset))

  /** Spi */
  def withMdio(addrOffset: BigInt = 0x20000, base: PeriphGen = () => Seq()) = () => base() ++ Seq(new MDIOPeripheral(addrOffset))

  /** Timer */
  def withTimer(addrOffset: BigInt = 0x30000, base: PeriphGen = () => Seq()) = () => base() ++ Seq(new TimerPeripheral(addrOffset))

  /** Timer */
  def withMac(addrOffset: BigInt = 0x40000, base: PeriphGen = () => Seq()) = () => base() ++ Seq(new MacPeripheral(addrOffset))

  /** TRNG */
  def withTRNG(addrOffset: BigInt = 0x50000, base: PeriphGen = () => Seq()) = () => base() ++ Seq(new TRNGPeripheral(addrOffset))
}
