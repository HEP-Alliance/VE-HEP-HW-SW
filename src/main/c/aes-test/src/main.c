#include "murax.h"
#include <stdint.h>
#include <string.h>

typedef struct {
  uint8_t *key;
  uint8_t *plain;
  uint8_t *cipher;
} tv;

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

void phex(char *label, char*x, int n) {
  char *alphabet = "0123456789abcdef";
  print(label);
  print(": ");
  for (int i = 0; i < n; i++) {
	    uart_write(UART,alphabet[(x[i]>>4)&0xf]);
	    uart_write(UART,alphabet[(x[i]>>0)&0xf]);
  }
	uart_write(UART,'\n');
}


// read out csr registers
#define csr_read(csr)                             \
  ({                                              \
       register unsigned long __v;                \
       __asm__ __volatile__ ("csrr %0, " #csr     \
                             : "=r" (__v));       \
       __v;                                       \
   })

uint64_t cpucycles(void)
{
  uint64_t lo = csr_read(mcycle);
  uint64_t hi = csr_read(mcycleh);
  return (hi << 32 ) | lo;
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
      unsigned long long q = (c >> 1) + (c >> 2);
      q = q + (q >> 4);
      q = q + (q >> 8);
      q = q + (q >> 16);
      q = q + (q >> 32);
      /* Now q = (1/10) */
      q = q >> 3;
      /* Since q contains an error due to the bits shifted out of the value, we
         only use it to determine the remainder. */
      unsigned long long r = c - ((q << 3) + (q << 1));
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
}

void main() {
    tv vector[5];
    #include "../tv.c"
    println("[+] AES KAT test.");
    for (int i = 1; i < 5; i++) {
      uint8_t buf[16];
      uint64_t start = cpucycles();
      AES_write(AES,vector[i].key,vector[i].plain);
      AES->CTRL |= 1;
      while(AES->STATUS != 1){
        asm volatile ("nop");
      }
      AES_read(AES,buf);
      uint64_t stop = cpucycles();
      print_int("aes128 one block",stop-start);
      println("");
      phex("key",vector[i].key,16);
      phex("plain",vector[i].plain,16);
      phex("cipher",vector[i].cipher,16);
      phex("cipher_result",buf,16);
      if (memcmp(buf,vector[i].cipher,16)) {
        println("[!] Test failed");
      }
      println("[-] ============== [-]");
    }
    // tell simulation to stop with Success
    GPIO_A->OUTPUT = 0; // 0 == sucess 1 == failure
    GPIO_A->OUTPUT_ENABLE = 1; // induce simulation to read the output
}

void irqCallback(){
}
