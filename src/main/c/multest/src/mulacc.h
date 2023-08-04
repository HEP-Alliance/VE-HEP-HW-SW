#ifndef MULACC_H
#define MULACC_H
#include <stdint.h>
static inline void mulacc_reset() {
  asm volatile(".insn r CUSTOM_0, 0x7, 0x7f, x0, x0, x0");
}
static inline void mulacc_add(uint32_t a, uint32_t b, int index) {
  int op = 1;
  asm volatile("MULHU x0,%0,%1"::"r"(a),"r"(b)); // do the multiplication to set the correct mulResult value
  asm volatile(".insn r CUSTOM_0, 0x6, 0x7f, x0, %0, %1" ::"r"(op),"r"(index));
  //.insn r opcode, func3, func7, rd, rs1, rs2
  //asm volatile(".insn r CUSTOM_0, 0x6, 0x7f, x0, %0, x0" :: "r"(address));
}
static inline int mulacc_read(int index) {
  int result;
  int op = 2;
  asm volatile(".insn r CUSTOM_0, 0x5, 0x7f, %0, %1, %2" :"=r"(result):"r"(op),"r"(index));
  return result;
}
#endif
