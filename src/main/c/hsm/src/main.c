#include "_TPM_Init_fp.h"
#include "murax.h"
#include "tis.h"
#include <Tpm.h>
#include <Global.h>
#include <Startup_fp.h>
#include <stddef.h>

#define b2u32(x) (((x)[0] << 24) | ((x)[1] << 16) | ((x)[2] << 8) | ((x)[3]))
void print(const char *str) {
  while (*str) {
    uart_write(UART, *str);
    str++;
  }
}

void println(const char *str) {
  print(str);
  uart_write(UART, '\n');
}



void phex(char *label, char *src, int n) {
  char *alphabet = "0123456789abcdef";
  print(label);
  print(": ");
  for (int i = 0; i < n; i++) {
    uart_write(UART, alphabet[(src[i] >> 4) & 0xf]);
    uart_write(UART, alphabet[src[i] & 0xf]);
  }
  print("\n");
}

size_t _write(int fd, void *src, size_t n) {
	for (int i = 0; i < n; i++)
		uart_write(UART,i[(char*)src]);
	return n;
}

#define TPM_MAX_RESPONSE 2048
uint8_t buf[TPM_MAX_RESPONSE] = {0};
void initCmd() {
	uint8_t cc[131] = {
  0x80, 0x01, 0x00, 0x00, 0x00, 0x83, 0x00, 0x00, 0x01, 0x31, 0x40, 0x00,
  0x00, 0x01, 0x00, 0x00, 0x00, 0x49, 0x02, 0x00, 0x00, 0x00, 0x00, 0x20,
  0xc1, 0x48, 0x03, 0xd5, 0x54, 0xd6, 0x05, 0xa9, 0xf0, 0x18, 0x54, 0x3c,
  0x70, 0xec, 0x84, 0x2a, 0x0c, 0x1a, 0xc3, 0xcc, 0xa7, 0xf5, 0xe0, 0x54,
  0x63, 0x8b, 0x32, 0xac, 0xb6, 0x74, 0x06, 0x79, 0x01, 0x00, 0x20, 0x9e,
  0xe5, 0x78, 0x5d, 0x82, 0x25, 0x71, 0xf6, 0x7d, 0xf8, 0x9c, 0xb8, 0xdc,
  0x51, 0xfa, 0x96, 0xad, 0x3e, 0x81, 0x53, 0xe6, 0x19, 0x53, 0x2b, 0x05,
  0x7e, 0xcc, 0x66, 0x63, 0x20, 0x93, 0xbb, 0x00, 0x04, 0x00, 0x00, 0x00,
  0x00, 0x00, 0x1a, 0x00, 0x01, 0x00, 0x0b, 0x00, 0x03, 0x00, 0x72, 0x00,
  0x00, 0x00, 0x06, 0x00, 0x80, 0x00, 0x43, 0x00, 0x10, 0x08, 0x00, 0x00,
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
};
	memcpy(buf,cc,131);
}

int cnt = 0;
int cntold = 0;


void main() {
  println("hello world!");

  TIS_init(&buf);
  g_inFailureMode = 0;
  TPM_Manufacture(0);
  _plat__Signal_PowerOn();
  PCRSimStart();
  PCRStartup(SU_RESET, 0);
  Startup_In startup_param = {
      .startupType = TPM_SU_CLEAR,
  };
  TPM2_Startup(&startup_param);
  g_DRTMHandle = TPM_RH_UNASSIGNED;
  uint32_t requestSize = 0;
  uint32_t responseSize = TPM_MAX_RESPONSE;
  uint8_t *request = g_TIS.fifo;
  uint8_t *response = g_TIS.fifo;
  volatile uint32_t sts;
  uint32_t old_sts = -1;
	//initCmd();
  while (1) {
start:
		//for (volatile int i = 0; i < 1000; i++) 
		//	asm volatile("nop");
		if ((g_TIS.sts[0]&(1<<7)) == 0) { // if valid bit is 0
     sts = fixSTS(TIS_STS_COMMAND_READY | 4 | (0xff << 8), 0);
		}
    if (sts != old_sts) {
      phex("STS changed to", g_TIS.sts, 4);
      old_sts = sts;
    }
    if ((g_TIS.sts[0] & TIS_STS_TPM_GO) != 0) {
      uint32_t requestSize = (((uint32_t)g_TIS.fifo[2]) << 24) |
                             (((uint32_t)g_TIS.fifo[3]) << 16) |
                             (((uint32_t)g_TIS.fifo[4]) << 8) |
                             (((uint32_t)g_TIS.fifo[5]) << 0);

			if (requestSize > 2048) {
				println("Error with command?!?");
      	phex("Received TPM command", g_TIS.fifo, 30);
				TIS_init(&buf);
				goto start;
			}

      phex("Received TPM command", g_TIS.fifo, requestSize);
      // create input for ExecuteCommand
      responseSize = TPM_MAX_RESPONSE;
      ExecuteCommand(requestSize, request, &responseSize, &response);
      // TODO: set g_TIS.fifo_fill for reading out response or change interface
      g_TIS.fifo_fill = 0;
      // put response into tis and block here
      phex("Command Response ready", g_TIS.fifo, responseSize);

      g_TIS.fifo_rsp_max = responseSize;
      g_TIS.sts[0] &= ~(TIS_STS_TPM_GO); // disable GO bit

      while (g_TIS.fifo_fill < g_TIS.fifo_rsp_max)
        sts = fixSTS(TIS_STS_DATA_AVAILABLE, 0); // set data available bit
	
      // reset conditions in the state machine
      g_TIS.fifo_fill = 0;
      g_TIS.fifo_rsp_max = TPM_MAX_RESPONSE;
			g_TIS.transfered_bytes = 0;
			sts = fixSTS(TIS_STS_COMMAND_READY, (1<<4)|(1<<5)|(1<<3));
    }

    sts |= TIS_STS_COMMAND_READY;
  }

  // tell simulation to stop with Success
  GPIO_A->OUTPUT = 0;        // 0 == sucess 1 == failure
  GPIO_A->OUTPUT_ENABLE = 1; // induce simulation to read the output
}

void interruptCtrl(int en) { asm volatile("csrw mie,%0" ::"r"(en)); }

void irqCallback() {
  interruptCtrl(0);
  cnt++;
  // register the interrupt handler here
  if (SPI->STATUS & (1 << 9)) {
    spi1_isr(); // this will assume there is at least one byte in the fifo!
  } else {
		println("Unknown interrupt detected. Disable now!");
	}
  interruptCtrl(0x880);
}
