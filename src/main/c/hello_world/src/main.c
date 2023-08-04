#include "murax.h"
#include <stdio.h>
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

int _write(int fd, const char *ptr, size_t len) {
	if (!ptr) return 0;
	if (fd != 1) return 0;
	for (int i = 0; i < len; i++)
		uart_write(UART,i[ptr]);
}

void phex(char *label, char *ptr, size_t len) {
	print(label);
	print(":");
	char *alpha = "0123456789abcdef";
	for (int i = 0; i < len; i++) {
		uart_write(UART, alpha[(ptr[i]>>4)&0xf]);
		uart_write(UART, alpha[ptr[i]&0xf]);
	}
	print("\n");
}

void interruptCtrl(int en) { asm volatile("csrw mie,%0" ::"r"(en)); }
void main() {
		interruptCtrl(0);
    println("1. Hello World!");
    println("2. Hello World!");
    println("3. Hello World!");
//		char * ptr = _sbrk(8);
//		phex("ptr",&ptr,4);
//    printf("Hello World!\n");

//    println("Hello World!");
//		char *x = malloc(100);
//		x[0] = 'A';
//		x[1] = 'A';
//		x[2] = 'A';
//		x[3] = '\n';
//		x[4] = '\0';
//		println(x);
    // tell simulation to stop with Success
    GPIO_A->OUTPUT = 0; // 0 == sucess 1 == failure
    GPIO_A->OUTPUT_ENABLE = 1; // induce simulation to read the outpu}
}
void irqCallback(){
}
