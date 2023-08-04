#include <stdint.h>
#include <string.h>
#include "murax.h"
#include "comba.h"
#include "P434_internal.h" 
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

int delay(uint32_t loops){
  volatile int tmp;
	for(int i=0;i<loops;i++){
		 tmp = GPIO_A->OUTPUT;
	}
  return tmp;
}

void * memcpy (void *dst, const void *src, size_t len) {
  for (int i = 0; i < len; i++)
    i[(char*)dst] = i[(char *)src];
  return dst;
}

void hex_print(uint32_t x) {
  const  char alphabet[16] = "0123456789ABCDEF";
  for (int i = 0; i < 8; i++) {
    uart_write(UART,alphabet[(x>>(4*(7-i)))&0xf]);
  }
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
  print("\n");
}


#define SIZE 4
void mul_comba(uint32_t a[SIZE], uint32_t b[SIZE], uint32_t c[2*SIZE]) {
    comba_shift();
    comba_shift();
    comba_mul(a[0],b[0]);
    c[0] = comba_shift();
    comba_mul(a[1],b[0]);
    comba_mul(a[0],b[1]);
    c[1] = comba_shift();
    comba_mul(a[0],b[2]);
    comba_mul(a[1],b[1]);
    comba_mul(a[2],b[0]);
    c[2] = comba_shift();
    comba_mul(a[0],b[3]);
    comba_mul(a[1],b[2]);
    comba_mul(a[2],b[1]);
    comba_mul(a[3],b[0]);
    c[3] = comba_shift();
    comba_mul(a[1],b[3]);
    comba_mul(a[2],b[2]);
    comba_mul(a[3],b[1]);
    c[4] = comba_shift();
    comba_mul(a[2],b[3]);
    comba_mul(a[3],b[2]);
    c[5] = comba_shift();
    comba_mul(a[3],b[3]);
    c[6] = comba_shift();
    c[7] = comba_shift();
}
void main() {
    uint32_t a[SIZE];
    uint32_t b[SIZE];
    uint32_t c[SIZE+SIZE];
    for (int i = 0; i < SIZE; i++) {
      a[i] = 0xffffffff;
      b[i] = 0xffffffff;
    }
    println("hello world!\n");
    uint64_t start = cpucycles();
    comba_shift();
    for (int i = 0; i < SIZE; i++){
      for (int j = 0; j <= i; j++)
        comba_mul(a[j],b[i-j]);
      c[i] = comba_shift();
    }
    for (int i = SIZE; i < 2*SIZE;i++){
      for (int j = i-SIZE+1; j < SIZE; j++)
        comba_mul(a[j],b[i-j]);
      c[i] = comba_shift();
    }
    uint64_t stop = cpucycles();
    print_int("\n128x128mul cycles start",start);
    print_int("\n128x128mul cycles stop",stop);
    print_int("\n128x128mul cycles",stop-start);
    start=cpucycles();
    mul_comba(a,b,c);
    stop = cpucycles();
    print_int("\n128x128mul cycles start",start);
    print_int("\n128x128mul cycles stop",stop);
    print_int("\n128x128mul cycles",stop-start);
    for (int i = (SIZE*2) -1; i >= 0; i--) {
      hex_print(c[i]);
    }
    println("\n\n");

    digit_t _a[NWORDS_FIELD*2];
    digit_t _b[NWORDS_FIELD*2];
    digit_t _c[2*NWORDS_FIELD*2];

    println("\n");
    start = cpucycles();
    mp_mul(a,b,c,NWORDS_FIELD);
    stop = cpucycles();

    print_int("\nmp_mul",stop-start);
    println("\n\n");
    //GPIO_A->OUTPUT = 0; // 0 == sucess 1 == failure
    //GPIO_A->OUTPUT_ENABLE = 1; // induce simulation to read the output
}//

void irqCallback(){
}
