#include <stdint.h>
#include <string.h>
#include "murax.h"
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



void print_int(const char* label, uint64_t c)
{
  /* Avoid printf */
  int i = 0;
  char outs[21] = {0};
  if (c < 10) {
    outs[0] = '0' + c;
  } else {
    i = 19;
    while (c != 0) {
      /* Method adapted from ""hackers delight":
         Creates an approximation of q = (8/10) */
      uint64_t q = (c >> 1) + (c >> 2);
      q = q + (q >> 4);
      q = q + (q >> 8);
      q = q + (q >> 16);
      q = q + (q >> 32);
      /* Now q = (1/10) */
      q = q >> 3;
      /* Since q contains an error due to the bits shifted out of the value, we
         only use it to determine the remainder. */
      uint64_t r = c - ((q << 3) + (q << 1));
      c = q;
      /* The remainder might be off by 10, so q may be off by 1 */
      if (r > 9) {
        c += 1;
        r -= 10;
      }
      outs[i] = '0' + (unsigned) r;
      i -= 1;
    }
    i += 1;
  }
  print(label);
  print(": ");
  print(outs + i);
  print("\n");
}


void itoa_test() {
//  uint64_t a[20] = {0L, 4L, 47L, 28L, 6189L, 86067L, 634561L, 9081047L, 57906124L, 6893473L, 3525249307L, 49678403702L, 603665794240L, 9908737417398L, 20007933162562LL, 943053361827648LL, 6292970125419564LL, 39114461081749410LL, 641084184801422806LL, 206250555373364017L};
//  char * ctrl [] = {
//    "0",
//"4",
//"47",
//"28",
//"6189",
//"86067",
//"634561",
//"9081047",
//"57906124",
//"6893473",
//"3525249307",
//"49678403702",
//"603665794240",
//"9908737417398",
//"20007933162562",
//"943053361827648",
//"6292970125419564",
//"39114461081749410",
//"641084184801422806",
//"206250555373364017"
//  }; 
  for (int i = 0; i < 20; i++) {
      print_int("test",i);
  }
}


void main() {
      println("hello world!\n");
      println("hello world!\n");
      println("hello world!\n");
      itoa_test();
			for (int i = 1; i < 1000; i+=67) 
				for (int j = 1; j < 100; j+=6) {
				print_int("a",i); 
				print_int("b",j);
				print_int("a/b",i/j); 
			}
      GPIO_A->OUTPUT = 0; // 0 == sucess 1 == failure
      GPIO_A->OUTPUT_ENABLE = 1; // induce simulation to read the output
}

void _exit() {

}

void irqCallback(){
}
