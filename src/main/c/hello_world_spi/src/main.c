#include "murax.h"
#include <string.h>

void print(const char*str){
	while(*str){
		uart_write(UART,*str);
		str++;
	}
}

void println(const char*str){
	print(str);
	uart_write(UART,'\n');
}

void main() {
    char *hw = "Hello World!\n";
    GPIO_A->OUTPUT_ENABLE = 0; // induce simulation to read the output
    println("Hello World!");
    char buf[100];
    memset(buf,'\n',sizeof(buf));
    int count=0;
    // enable reception
    SPI->STATUS |= (1<<15);
    SPI->CONFIG = 0;
    //SPI->DATA = 'A';
    count = 0;
    while(1) {
      uint32_t data = SPI->DATA;
      int rxavail = ((data)>>16)&0x3fff;
      int rxvalid = ((data)>>31);
      if (rxavail>0) {
          uint8_t byte = data&0xff;
          SPI->DATA = byte;
          if (count == 13) goto exit;
      }
    }
exit:
    println("[+] exiting...\n");
    // tell simulation to stop with Success
    GPIO_A->OUTPUT = 0; // 0 == sucess 1 == failure
    GPIO_A->OUTPUT_ENABLE = 1; // induce simulation to read the output
}

void irqCallback(){
}
