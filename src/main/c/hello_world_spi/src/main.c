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
    int count=0;
    // enable reception
    SPI->STATUS = (1<<15) | (1<<1);	
	
    SPI->CONFIG = 0;
		SPI->DATA = 'A' & 0xff;
		SPI->DATA = 'A' & 0xff;
		SPI->DATA = 'A' & 0xff;
		SPI->DATA = 'A' & 0xff;
    count = 0;
    while(1) {
/*      uint32_t data = SPI->DATA;
      int rxavail = (((data)>>16)&((int)0x7fff));
      int rxvalid = ((data)>>31)&1;
      if ((rxavail>0) || (rxvalid)) {
          uint8_t byte = data&0xff;
					while(((((SPI->STATUS)>>16) & 0x7fff) == 0));
          SPI->DATA = byte;
      }
*/
    }
exit:
		GPIO_A->OUTPUT = 0;
		GPIO_A->OUTPUT_ENABLE = 1;
}

void interruptCtrl(int en) { asm volatile("csrw mie,%0" ::"r"(en)); }


void irqCallback(){
	interruptCtrl(0x00);
  
  if (SPI->STATUS & (1 << 9)) {
  	volatile uint8_t byte = SPI->DATA;
	  SPI->DATA  = byte;
	}
	
	interruptCtrl(0x880);
}
