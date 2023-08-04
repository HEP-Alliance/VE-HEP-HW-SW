#ifndef MULACC_H
#define MULACC_H
#include <stdint.h>
static inline void comba_add(uint32_t a, uint32_t b) {
  asm volatile(".insn r CUSTOM_0, 0x6, 0x7f, x0, %0, %1" ::"r"(a),"r"(b));
}
static inline void comba_mul(register uint32_t a,register uint32_t b) {
  asm volatile(".insn r CUSTOM_0, 0x5, 0x7f, x0, %0, %1" ::"r"(a),"r"(b));
}
static inline int comba_shift() {
  register uint32_t result;
  asm volatile(".insn r CUSTOM_0, 0x7 , 0x7f, %0, x0, x0" :"=r"(result):);
  return result;
}
#endif
