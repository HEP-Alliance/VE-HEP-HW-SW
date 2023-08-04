#include "tis.h"
#include "murax.h"
#include <string.h>
volatile TIS g_TIS;

char vid[3] = {0xc0, 0xff, 0xee, 0xaa};

// initialize the TIS state to sane start values
void TIS_init(uint8_t *fifo) {
  // entable SPI interrupts and receiption
  SPI->STATUS |= (1 << 15); // rxlisten
  SPI->STATUS |= (1 << 1);  // rxIntEnable
  SPI->CONFIG = 0x0;
  g_TIS.fifo = fifo;
  g_TIS.isRead = 0;
  g_TIS.nbytes = 0;
  g_TIS.lastbyte = 0;
  g_TIS.address = 0;
  g_TIS.addrbytes = 0;
  g_TIS.transfered_bytes = 0;
  g_TIS.fifo_rsp_max = 0;
  g_TIS.access = 0x80 + (1 << 5);
  memset(g_TIS.sts, 0x00, sizeof(g_TIS.sts));
  g_TIS.sts[0] = TIS_STS_VALID | TIS_STS_COMMAND_READY;
  SPI->DATA = (uint8_t)0x00;
}

// helper function for the interrupt driven communication routine
static void read_write_spi(uint8_t *data, uint8_t byte) {
  if (g_TIS.isRead == 0) {
    SPI->DATA = (uint8_t)0x00;
    *data = byte;
  } else {
    SPI->DATA = (uint8_t)*data;
  }
}

void spi1_isr() {
  // receive the current byte
  uint8_t byte = SPI->DATA;
  if (g_TIS.nbytes == 0) {
    // parse first byte of transaction
    if (g_TIS.lastbyte && g_TIS.isRead) {
      g_TIS.lastbyte = 0;
      g_TIS.transfered_bytes = 0;
    } else {
      g_TIS.nbytes = (byte & 0x3f) + 1;
      g_TIS.transfered_bytes = 0;
    }
    g_TIS.isRead = (byte >> 7) & 1;
    g_TIS.address = 0;
    g_TIS.addrbytes = 0;
    SPI->DATA = (uint8_t)0x00;
  } else {
    if (g_TIS.addrbytes < 3) {
      // parse bytes 1/2/3 for address
      g_TIS.address = (g_TIS.address << 8) | byte;
      g_TIS.addrbytes++;
      if (g_TIS.addrbytes < 3)     // after the ++ it is still smaller
        SPI->DATA = (uint8_t)0x00; // answer with the first payload byte
      else if (g_TIS.isRead == 0)
        return;
    }
  }
  if (g_TIS.addrbytes == 3) {
    g_TIS.nbytes--;
    if (g_TIS.nbytes == 0) {
      if (g_TIS.isRead)
        g_TIS.lastbyte = 1;
      g_TIS.lastaddress = (g_TIS.lastaddress << 10) | g_TIS.address;
      g_TIS.addrbytes = 0;
      g_TIS.transfered_bytes = 0;
    }
    switch ((g_TIS.address)) {
    case ACCESS_ADDRESS:
      // read_write_spi(&g_TIS.access,byte);
      SPI->DATA = 0xa0;
      break;
    case STS_ADDRESS:
      read_write_spi(&(g_TIS.sts[g_TIS.transfered_bytes]), byte);
			// if it is a write we need to dirty up the sts
			if (!g_TIS.isRead) { 
				// we ensure that any write to this address unsets the valid bit (1<<7)
				g_TIS.sts[0] &= 0x7f; // this will unset the valid bit
			}	
      break;
    case FIFO_ADDRESS:
      read_write_spi(&(g_TIS.fifo[g_TIS.fifo_fill]), byte);
      g_TIS.fifo_fill++;
      if ((g_TIS.fifo_fill >= g_TIS.fifo_rsp_max) && (g_TIS.fifo_rsp_max > 0))
        g_TIS.sts[0] &= ~(TIS_STS_DATA_AVAILABLE);
      break;
    case VID_ADDRESS:
      SPI->DATA = vid[g_TIS.transfered_bytes];
      break;
		case RID_ADDRESS:
      SPI->DATA = 0x55;
      break;
    default:
      SPI->DATA = (uint8_t)0x00;
    }
    g_TIS.transfered_bytes++;
  }
}

///#if 0
///// this is the interrupt handler for TIS on the stm32f4
/// void spi1_isr() {
///  uint8_t byte = SPI->DATA;
///  if (g_TIS.nbytes > 0) {
///	readWrite:
///    if (g_TIS.addrbytes == 3) {
///      switch((g_TIS.address)) {
///        case STS_ADDRESS:
///          read_write_spi(&(g_TIS.sts[g_TIS.transfered_bytes]), byte);
///          break;
///        case FIFO_ADDRESS:
///          read_write_spi(&(g_TIS.fifo[g_TIS.fifo_fill]), byte);
///          g_TIS.fifo_fill ++;
///          if ((g_TIS.fifo_fill >= g_TIS.fifo_rsp_max) && (g_TIS.fifo_rsp_max
///          > 0))
///            g_TIS.sts[0] &= ~(TIS_STS_DATA_AVAILABLE);
///          break;
///       	case VID_ADDRESS:
///          SPI->DATA = vid[g_TIS.transfered_bytes];
///          break;
///        case ACCESS_ADDRESS:
///				  //read_write_spi(&g_TIS.access,byte);
///				  SPI->DATA = (uint8_t)g_TIS.access | 0x80;
///          break;
///        default:
///          SPI->DATA=0x80|(1<<5);
///      }
///      g_TIS.nbytes --;
///      g_TIS.transfered_bytes ++;
///      if (g_TIS.nbytes == 0) {
///        g_TIS.lastaddress = (g_TIS.lastaddress << 10) | g_TIS.address;
///        g_TIS.addrbytes = 0;
///        g_TIS.transfered_bytes = 0;
///      }
///    } else {
///      g_TIS.address = (g_TIS.address<<8)|byte;
///      g_TIS.addrbytes++;
/////      SPI->DATA=(uint8_t)0x00;
///			if(g_TIS.addrbytes == 3)
///				goto readWrite;
///    }
///  } else {
///    // read/write and length
///    g_TIS.nbytes = (byte & 0x3f)+1;
///    g_TIS.isRead = (byte >> 7)&1;
///    g_TIS.address = 0;
///    g_TIS.addrbytes = 0;
///    SPI->DATA=(uint8_t)0x00;
///  }
///}
///#endif

// fixSTS(set,unset)
// INPUT: set
// INPUT: unset
// OUTPUT: current value of STS for main()
//
// This function fetches the current value of g_TIS.sts
// and modifies it according to set and unset and returns
// the result.
// To do this it needs a static variable to store the last
// version because the TIS interface might override
// the entire STS except for 1 bit.
//

#define bytesToU32(x) (x[0] | (x[1] << 8) | (x[2] << 16) | (x[3] << 24))
#define u32ToBytes(x, v)                                                       \
  {                                                                            \
    (x)[0] = (v)&0xff;                                                         \
    (x)[1] = ((v) >> 8) & 0xff;                                                \
    (x)[2] = ((v) >> 16) & 0xff;                                               \
    (x)[3] = ((v) >> 24) & 0xff;                                               \
  }

uint32_t fixSTS(uint32_t set, uint32_t unset) {
  static uint32_t value = 0;

  const uint32_t onemask = (1 << 7);
  const uint32_t zeromask = 1;

  // read out g_TIS.sts
  uint32_t current_value = bytesToU32(((volatile uint8_t *)g_TIS.sts));
  value |= current_value;
  value |= set;
  value &= ~(unset);

  // set the valid bit
  value |= (1 << 7);

  // write back to g_TIS.sts using bitmasks that only allow certain bits to be 1
  // or 0
  u32ToBytes(g_TIS.sts, ((value | onemask)));

  return value;
}
