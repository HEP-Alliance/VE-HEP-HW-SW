#ifndef TIS_H
#define TIS_H
#include <stdint.h>
//##include <libopencm3/stm32/spi.h>
typedef struct s_TIS {
  uint8_t access;
  uint8_t sts[4]; // upper most byte for streaming status, read as uint32_t
                  // 0 == nothing 1 == TPM wants to stream out 2 == TPM expects input
  uint8_t dummy[0x100];
  uint8_t int_cap[4];
  uint8_t *fifo;
  uint32_t fifo_fill;
  uint32_t fifo_rsp_max;
  uint8_t  io_stream[0x1000]; 
  uint32_t io_stream_fill; // if written to it gets incremented
  uint32_t io_stream_pos;
  uint32_t address;
  uint64_t lastaddress;
  int isRead;
	int lastbyte;
  int reads;
  int writes;
  int nbytes;
  int addrbytes;
  int transfered_bytes;
} TIS;
// reset state to default values
void spi1_isr();
void TIS_init(uint8_t *fifobuffer); 
extern volatile TIS g_TIS;
// macros to access the state
/////////////////////////////
// FROM THE TIS SPEC (this is not the complete list but suffices for demonstrator)
//0000h TPM_ACCESS_0 Used to gain ownership of the TPM for this particular Locality. 
//0017h-0014h TPM_INTF_CAPABILITY_0 Provides the information about supported interrupts and the characteristic of the burstCount register of the particular TPM
//  we have to set this part such that nobody tries to enable any interrupts ... TODO: research proper init values
//
//001Ah-0018h TPM_STS_0 Status Register. Provides status of the TPM
//00027h-0024h TPM_DATA_FIFO_0 ReadFIFO or WriteFIFO, depending on the current bus cycle (read or write).
//#define DUMMY_ADDRESS               0x2200 ... 0x2300
//#define ACCESS_ADDRESS              0x0000 ... 0x0007
//#define INT_CAP_ADDRESS             0x0014 ... 0x0017
//#define STS_ADDRESS                 0x0018 ... 0x001A
//#define FIFO_ADDRESS                0x0024 ... 0x0027
//


#define LOC_0                       0x00D40000
//#define LOC_0                       0x00
#define ACCESS_ADDRESS             ((LOC_0)+ 0x0000)
#define INT_CAP_ADDRESS            ((LOC_0)+ 0x0014)
#define STS_ADDRESS                ((LOC_0)+ 0x0018)
#define FIFO_ADDRESS               ((LOC_0)+ 0x0024)
#define VID_ADDRESS                ((LOC_0)+ 0x0f00)
#define RID_ADDRESS                ((LOC_0)+ 0x0f04)




#define TIS_DUMMY &(g_TIS->dummy)
#define TIS_ACCESS &(g_TIS->access)
#define TIS_INT_CAP &(g_TIS->int_cap)
#define TIS_STS   &(g_TIS->sts)
#define TIS_FIFO  &(*(g_TIS->fifo))
// STS lowest bits
// 0 reserved must be 0
// 1 responseRetry (reset fifo_fill for response)
// 2 selfTestDone
// 3 Expect The TPM sets this field to a value of “1” when it expects another byte of data for a command. It clears this field to a value of “0” when it has received all the data it expects for that command, based on the TPM size field within the packet.  Valid indicator: TPM_STS_x.stsValid = ‘1’ 
// 4 dataAvail This field indicates that the TPM has data available as a response. When set to “1”, software MAY read the ReadFIFO. The TPM MUST clear the field to “0” when it has returned all the data for the response. 
// 5 tpmGo After software has written a command to the TPM and sees that it was received correctly, software MUST write a “1” to this field to cause the TPM to execute that command. 
// 6 commandReady Read of ‘1’  indicates TPM is ready, Write of  ‘1’ causes TPM to transistion its state. Valid indicator: N/A
// 7 stsValid This field indicates that TPM_STS_x.dataAvail and TPM_STS_x.Expect are valid.  Valid indicator: N/A
// 8-23 burstCount Indicates the number of bytes that the TPM can return on reads or accept on writes without inserting wait states on the bus. 
// 24-31 (NEW STREAM bits)
// 24 outStream TPM wants to stream out data and will wait for the data to be read by the software
// 25 inStream  TPM requires input stream data and will wait for the data to be written by the software
// 26-31 reserved
#define TIS_STS_U32 (\
    (((uint32_t)(g_TIS.sts[0]) <<  0)) | \
    (((uint32_t)(g_TIS.sts[1]) <<  8)) | \
    (((uint32_t)(g_TIS.sts[2]) << 16)) | \
    (((uint32_t)(g_TIS.sts[3]) << 24)))

#define TIS_STS_SET_U32(x) { \
    g_TIS.sts[0] = (uint8_t)((x >>  0) & 0xff); \
    g_TIS.sts[1] = (uint8_t)((x >>  8) & 0xff); \
    g_TIS.sts[2] = (uint8_t)((x >> 16) & 0xff); \
    g_TIS.sts[3] = (uint8_t)((x >> 24) & 0xff); \
}

#define TIS_STS_EXPECT (1<<3)
#define TIS_STS_DATA_AVAILABLE (1<<4)
#define TIS_STS_TPM_GO (1<<5)
#define TIS_STS_COMMAND_READY (1<<6)
#define TIS_STS_VALID (1<<7)

// IOSTREAM STS constansts
#define TIS_STS_IO_STREAM_WANT_READ  1
#define TIS_STS_IO_STREAM_WANT_WRITE 2
#endif
