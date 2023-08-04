/********************************************************************************************
* Supersingular Isogeny Key Encapsulation Library
*
* Abstract: API header file for P434
*********************************************************************************************/  

#ifndef __P434_API_H__
#define __P434_API_H__
    

/*********************** Key encapsulation mechanism API ***********************/

#define CRYPTO_SECRETKEYBYTES     239      // MSG_BYTES + SECRETKEY_A_BYTES + CRYPTO_PUBLICKEYBYTES bytes
#define CRYPTO_PUBLICKEYBYTES     196      // 3*ORDER_B_ENCODED_BYTES + FP2_ENCODED_BYTES + 2 bytes for shared elligator
#define CRYPTO_BYTES               16
#define CRYPTO_CIPHERTEXTBYTES    209      // COMPRESSED_CHUNK_CT + MSG_BYTES bytes       


// Algorithm name
#define CRYPTO_ALGNAME "SIKEp434_compressed"  

// SIKE's key generation
// It produces a private key sk and computes the public key pk.
// Outputs: secret key sk                     (CRYPTO_SECRETKEYBYTES = 239 bytes)
//          compressed public key pk          (CRYPTO_PUBLICKEYBYTES = 196 bytes) 
int crypto_kem_keypair(unsigned char *pk, unsigned char *sk);

// SIKE's encapsulation
// Input:   compressed public key pk          (CRYPTO_PUBLICKEYBYTES = 196 bytes)
// Outputs: shared secret ss                  (CRYPTO_BYTES = 16 bytes)
//          compressed ciphertext message ct  (CRYPTO_CIPHERTEXTBYTES = 209 bytes)
int crypto_kem_enc(unsigned char *ct, unsigned char *ss, const unsigned char *pk);

// SIKE's decapsulation
// Input:   secret key sk                     (CRYPTO_SECRETKEYBYTES = 239 bytes)
//          compressed ciphertext message ct  (CRYPTO_CIPHERTEXTBYTES = 209 bytes) 
// Outputs: shared secret ss                  (CRYPTO_BYTES = 16 bytes)
int crypto_kem_dec(unsigned char *ss, unsigned char *ct, const unsigned char *sk);


// Encoding of keys for KEM-based isogeny system "SIKEp434_compressed" (wire format):
// ----------------------------------------------------------------------
// Elements over GF(p434) are encoded in 55 octets in little endian format (i.e., the least significant octet is located in the lowest memory address). 
// Elements (a+b*i) over GF(p434^2), where a and b are defined over GF(p434), are encoded as {a, b}, with a in the lowest memory portion.
//
// Private keys sk consist of the concatenation of a 16-byte random value, a value in the range [0, 2^217-1] and the public key pk. In the SIKE API, 
// private keys are encoded in 239 octets in little endian format. 
// Public keys pk consist of 3 elements in Z_Bob_order plus one in GF(p610^2) plus 2 bytes. In the SIKE API, pk is encoded in 196 octets.
// Ciphertexts ct consist of the concatenation of a public key value and a 16-byte value. In the SIKE API, ct is encoded in 209 octets.  
// Shared keys ss consist of a value of 16 octets.


/*********************** Ephemeral key exchange API ***********************/

#define SIDH_SECRETKEYBYTES      27
#define SIDH_PUBLICKEYBYTES     196
#define SIDH_BYTES              110

// SECURITY NOTE: SIDH supports ephemeral Diffie-Hellman key exchange. It is NOT secure to use it with static keys.
// See "On the Security of Supersingular Isogeny Cryptosystems", S.D. Galbraith, C. Petit, B. Shani and Y.B. Ti, in ASIACRYPT 2016, 2016.
// Extended version available at: http://eprint.iacr.org/2016/859  

// Generation of Alice's secret key 
// Outputs random value in [0, 2^216 - 1] to be used as Alice's private key
void random_mod_order_A(unsigned char* random_digits);

// Generation of Bob's secret key 
// Outputs random value in [0, 2^Floor(Log(2,3^137)) - 1] to be used as Bob's private key
void random_mod_order_B(unsigned char* random_digits);

// Alice's ephemeral public key generation
// Input:  a private key PrivateKeyA in the range [0, 2^216 - 1], stored in 27 bytes. 
// Output: the public key PublicKeyA consisting of 3 GF(p434^2) elements encoded in 330 bytes.
int EphemeralKeyGeneration_Compressed_A(const unsigned char* PrivateKeyA, unsigned char* PublicKeyA);

// Bob's ephemeral key-pair generation
// It produces a private key PrivateKeyB and computes the public key PublicKeyB.
// The private key is an integer in the range [0, 2^Floor(Log(2,3^137)) - 1], stored in 28 bytes. 
// The public key consists of 3 GF(p434^2) elements encoded in 330 bytes.
int EphemeralKeyGeneration_Compressed_B(const unsigned char* PrivateKeyB, unsigned char* PublicKeyB);

// Alice's ephemeral shared secret computation
// It produces a shared secret key SharedSecretA using her secret key PrivateKeyA and Bob's compressed public key PublicKeyB
// Inputs: Alice's PrivateKeyA is an integer in the range [0, 2^216 - 1], stored in 27 bytes. 
//         Bob's PublicKeyB consists of 3 GF(p434^2) elements encoded in 330 bytes.
// Output: a shared secret SharedSecretA that consists of one element in GF(p434^2) encoded in 110 bytes.
int EphemeralSecretAgreement_Compressed_A(const unsigned char* PrivateKeyA, const unsigned char* PublicKeyB, unsigned char* SharedSecretA);

// Bob's ephemeral shared secret computation
// It produces a shared secret key SharedSecretB using his secret key PrivateKeyB and Alice's public key PublicKeyA
// Inputs: Bob's PrivateKeyB is an integer in the range [0, 2^Floor(Log(2,3^137)) - 1], stored in 28 bytes. 
//         Alice's PublicKeyA consists of 3 GF(p434^2) elements encoded in 330 bytes.
// Output: a shared secret SharedSecretB that consists of one element in GF(p434^2) encoded in 110 bytes.
int EphemeralSecretAgreement_Compressed_B(const unsigned char* PrivateKeyB, const unsigned char* PublicKeyA, unsigned char* SharedSecretB);


// Encoding of keys for KEX-based isogeny system "SIDHp434" (wire format):
// ----------------------------------------------------------------------
// Elements over GF(p434) are encoded in 55 octets in little endian format (i.e., the least significant octet is located in the lowest memory address). 
// Elements (a+b*i) over GF(p434^2), where a and b are defined over GF(p434), are encoded as {a, b}, with a in the lowest memory portion.
//
// Private keys PrivateKeyA and PrivateKeyB can have values in the range [0, 2^216-1] and [0, 2^217-1], resp. In the SIDH API, private keys are encoded 
// in 28 octets in little endian format. 
// Compressed Public keys PublicKeyA and PublicKeyB consist of 3 elements in {Z_2^OALICE_BITS, Z_3^OBOB_EXPON}. In the SIDH API, they are encoded in 193 octets. 
// Shared keys SharedSecretA and SharedSecretB consist of one element in GF(p434^2). In the SIDH API, they are encoded in 110 octets.


#endif