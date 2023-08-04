#ifndef SPI_H_
#define SPI_H_

  /*
   * In short, it has one read fifo and one write fifo.
   * data -> 0x00 :
   * - rxTxData -> RW[7:0]
   * - rxOccupancy -> R[30:16]
   * - rxValid -> R[31]
   * - When you read this register it pop an byte of the rx fifo (mosi) and provide its value (via rxTxData)
   * - When you write this register, it push a byte into the tx fifo (miso).
   *
   * status -> 0x04 :
   * - txIntEnable -> RW[0]
   * - rxIntEnable -> RW[1]
   * - ssEnabledIntEnable -> RW[2]
   * - ssDisabledIntEnable -> RW[3]
   * - txInt -> R[8] Interruption which occur when the tx fifo is empty
   * - rxInt -> R[9] Interruption which occur when the rx fifo is not empty
   * - ssEnabledInt -> R[10] Interruption which occur when the SPI interface is selected from the master (ss falling edge).
   * - ssDisabledInt -> R[11] Interruption which occur when the SPI interface is deselected from the master (ss rising edge).
   * - ssEnabledIntClear -> W[12] When set, clear the ssEnabledInt interrupt
   * - ssDisabledIntClear -> W[13] When set, clear the ssDisabledInt interrupt
   * - rxListen -> RW[15] Enable the reception of mosi bytes
   * - txAvailability -> R[30:16] Space avalaible in the tx fifo
   *
   * config -> 0x08
   * - cpol -> W[0]
   * - cpha -> W[1]
   **/

typedef struct
{
  volatile uint32_t DATA;
  volatile uint32_t STATUS;
  volatile uint32_t CONFIG;
} SPI_Reg;


#endif /* UART_H_ */


