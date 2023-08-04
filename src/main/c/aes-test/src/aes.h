#ifndef AES_H_
#define AES_H_
#include <stdint.h>

typedef struct
{
  volatile uint32_t STATUS;
  volatile uint32_t CTRL;
  volatile uint32_t KEY;
  volatile uint32_t PLAIN;
  volatile uint32_t CIPHER;
} Aes_Reg;

void AES_write(Aes_Reg *reg, uint8_t *key, uint8_t *plain) {
    uint32_t key_data = 0;
    uint32_t plain_data = 0;
    for (int i = 0; i < 16; i++) {
      key_data = (key_data<<8) | key[i];
      plain_data = (plain_data<<8) | plain[i];
      if ((i&3) == 3) {
        reg -> KEY = key_data;
        reg -> PLAIN = plain_data;
      }
    }
}

void AES_read(Aes_Reg *reg, uint8_t *cipher) {
    for (int i = 0; i < 4; i++) {
      uint32_t cipher_data = reg -> CIPHER;
      cipher[(i<<2) + 3] = (cipher_data >>  0) & 0xff;
      cipher[(i<<2) + 2] = (cipher_data >>  8) & 0xff;
      cipher[(i<<2) + 1] = (cipher_data >> 16) & 0xff;
      cipher[(i<<2) + 0] = (cipher_data >> 24) & 0xff;
    }
}
#endif /* AES_H_ */
