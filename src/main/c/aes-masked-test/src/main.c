#include "murax.h"
#include <stdint.h>
#include <string.h>
#include "fips202.h"

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

#define ROTL(a,b) (((a) << (b)) | ((a) >> (32 - (b))))
#define QR(a, b, c, d) (\
  a += b,  d ^= a,  d = ROTL(d,16),\
    c += d,  b ^= c,  b = ROTL(b,12),\
    a += b,  d ^= a,  d = ROTL(d, 8),\
  c += d,  b ^= c,  b = ROTL(b, 7))
#define ROUNDS 20

void chacha_block(uint32_t out[16], uint32_t const in[16])
{
  int i;
  uint32_t x[16];

  for (i = 0; i < 16; ++i)
    x[i] = in[i];
  // 10 loops × 2 rounds/loop = 20 rounds
  for (i = 0; i < ROUNDS; i += 2) {
    // Odd round
    QR(x[0], x[4], x[ 8], x[12]); // column 0
    QR(x[1], x[5], x[ 9], x[13]); // column 1
    QR(x[2], x[6], x[10], x[14]); // column 2
    QR(x[3], x[7], x[11], x[15]); // column 3
    // Even round
    QR(x[0], x[5], x[10], x[15]); // diagonal 1 (main diagonal)
    QR(x[1], x[6], x[11], x[12]); // diagonal 2
    QR(x[2], x[7], x[ 8], x[13]); // diagonal 3
    QR(x[3], x[4], x[ 9], x[14]); // diagonal 4
  }
  for (i = 0; i < 16; ++i)
    out[i] = x[i] + in[i];
}

void main() {

  uint32_t prng[17];
  uint32_t rng[16];
  prng[16] = 16;
  memset(prng,'X',16*sizeof(uint32_t));
  
  
  
  uint8_t out[20];
  shake128incctx state;
  // shake128_inc_init(&state);
  // shake128_inc_absorb(&state,"seed",4);
  // shake128_inc_finalize(&state);
  // shake128_inc_squeeze(out,20,&state);
  phex("shake('seed')[0:20]",out,20);
      uint8_t maskedpt[16] = {0};
      uint8_t maskedkey[16] = {0};
      tv vector[5];
#include "../tv.c"
    println("[+] AES KAT test.");
    for (int i = 0; i < 5; i++) {
      uint8_t buf1[16];
      uint8_t buf2[16];
      uint64_t start = cpucycles();
      
      // shake128_inc_squeeze(maskedpt,16,&state);
      //shake128_inc_squeeze(maskedkey,16,&state);
      chacha_block(rng,prng);
      memcpy(rng,maskedkey,16);
      memcpy(rng+4,maskedpt,16);
      prng[16]=8;
      AES_write(AES,maskedkey,maskedpt);
      for (int j = 0; j < 16; j++) {
	maskedpt[j] ^= vector[i].plain[j];
	maskedkey[j] ^= vector[i].key[j];
      }
      AES_write(AES,maskedkey,maskedpt);
      for (int j = 0; j < 270; j++) {
        if (prng[16] == 0) {
	  prng[16] = 16;
	  prng[0]++;
	  chacha_block(rng,prng);
        }
        AES->MASK = rng[16-prng[16]];
        prng[16]--;
      }
      AES->CTRL |= 1;
      while(AES->STATUS != 1){
        asm volatile ("nop");
      }
      AES_read(AES,buf1);
      AES_read(AES,buf2);
      for (int j = 0; j < 16; j++)
	buf1[j] ^= buf2[j];
      uint64_t stop = cpucycles();
      print_int("aes128 one block",stop-start);
      println("");
      phex("key",vector[i].key,16);
      phex("plain",vector[i].plain,16);
      phex("cipher",vector[i].cipher,16);
      phex("cipher_result",buf1,16);
      if (memcmp(buf1,vector[i].cipher,16)) {
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
