package ap5

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.com.eth._
import spinal.lib.com.uart._
import spinal.lib.misc.Clint
import spinal.lib.io._
import spinal.lib.com.spi.ddr._
import spinal.core.sim._
import spinal.lib.com.spi.{Apb3SpiSlaveCtrl,SpiSlave,SpiSlaveCtrlGenerics,SpiSlaveCtrlMemoryMappedConfig}
import mupq._

class SpiSlavePeripheral(val addrOffset: BigInt = 0xA0000) extends Peripheral[PQVexRiscv] {
  var spi: SpiSlave = null

  override def setup(soc: PQVexRiscv) = {
    spi = master(SpiSlave(true)) setName("spi")
  }

  override def build(soc: PQVexRiscv) = {
    soc plug new ClockingArea(soc.systemClockDomain) {

      val spiCtrl = new Apb3SpiSlaveCtrl(SpiSlaveCtrlMemoryMappedConfig(SpiSlaveCtrlGenerics(8)))
      spi <> spiCtrl.io.spi
      soc.core.externalInterrupt setWhen (spiCtrl.io.interrupt)
      soc.apbSlaves += spiCtrl.io.apb -> (addrOffset, 64 KiB)
    }
  }
}

object SpiPeripherals {
  def withSpiSlave(base: () => Seq[Peripheral[PQVexRiscv]]) = 
    () => base() ++ Seq(new SpiSlavePeripheral())
}
