package ap5

import java.io.{InputStream, OutputStream}
import java.net.ServerSocket

import spinal.core.sim._
import spinal.lib.com.spi.SpiSlave


object SpiTcp {
   def apply(spi: SpiSlave, SpiClkPeriod: Long) = fork {
    var inputStream: InputStream = null
    var outputStream: OutputStream = null

    class SocketThread extends Thread  {
      val socket = new ServerSocket(7895)
      override def run() : Unit = {
        println("WAITING FOR TCP Spi CONNECTION")
        while (true) {
          val connection = try { socket.accept() } catch { case e : Exception => return }
          connection.setTcpNoDelay(true)
          outputStream = connection.getOutputStream()
          inputStream = connection.getInputStream()
        //  println("TCP Spi CONNECTION")
        }
      }
    }
    val server = new SocketThread
    onSimEnd (server.socket.close())
    server.start()
    spi.sclk #= false;

     var out = 0;
     sleep(SpiClkPeriod);
    while (true) {
      sleep(SpiClkPeriod * 200)
			try{
      while (inputStream != null && inputStream.available() != 0) {
        val buffer = inputStream.read()
        out = 0;
        spi.ss   #= false;
        for (i <- Range(0,8).reverse) {
          // sampling miso
          if (spi.miso.write.toBoolean == true)
            out = out | (1 << i)
          val bit = (buffer>>i)&1;
          // edge rise
          spi.mosi #= bit != 0;
          spi.sclk #= true;
          sleep(SpiClkPeriod / 4)
           // edge fall
          sleep(SpiClkPeriod / 4)
           spi.sclk #= false;
          sleep(SpiClkPeriod / 4)
          sleep(SpiClkPeriod / 4)
        }
        spi.ss   #= true;
        outputStream.write(out&0xff);

//        spi.sclk #= (buffer & 0x1) != 0;
//        spi.mosi #= (buffer & 0x2) != 0;
//        spi.ss   #=  false;  
//        if ((buffer & 0x1) != 0) {
//          outputStream.write(if (spi.miso.write.toBoolean) 49 else 48)
//        }
//        sleep(SpiClkPeriod / 2)
      }}
			catch {case e : Exception => println(e)}
    }
  }
}
