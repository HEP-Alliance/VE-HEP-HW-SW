VPATH += ./mstpm/TPMCmd
TPM_SRCS += Platform/src/Cancel.c \
Platform/src/DebugHelpers.c Platform/src/Entropy.c \
Platform/src/LocalityPlat.c Platform/src/NVMem.c Platform/src/PPPlat.c \
Platform/src/PlatformACT.c Platform/src/PlatformData.c Platform/src/PowerPlat.c \
Platform/src/RunCommand.c  \
Platform/src/Unique.c \
tpm/src/X509/TpmASN1.c \
tpm/src/command/Asymmetric/ECC_Decrypt.c \
tpm/src/command/Asymmetric/ECC_Encrypt.c \
tpm/src/command/Asymmetric/ECC_Parameters.c \
tpm/src/command/Asymmetric/ECDH_KeyGen.c tpm/src/command/Asymmetric/ECDH_ZGen.c \
tpm/src/command/Asymmetric/EC_Ephemeral.c \
tpm/src/command/Asymmetric/RSA_Decrypt.c \
tpm/src/command/Asymmetric/RSA_Encrypt.c \
tpm/src/command/Asymmetric/ZGen_2Phase.c \
tpm/src/command/AttachedComponent/AC_GetCapability.c \
tpm/src/command/AttachedComponent/AC_Send.c \
tpm/src/command/AttachedComponent/AC_spt.c \
tpm/src/command/AttachedComponent/Policy_AC_SendSelect.c \
tpm/src/command/Attestation/Attest_spt.c tpm/src/command/Attestation/Certify.c \
tpm/src/command/Attestation/GetCommandAuditDigest.c \
tpm/src/command/Attestation/GetSessionAuditDigest.c \
tpm/src/command/Attestation/GetTime.c tpm/src/command/Attestation/Quote.c \
tpm/src/command/Capability/GetCapability.c \
tpm/src/command/Capability/TestParms.c \
tpm/src/command/ClockTimer/ACT_SetTimeout.c \
tpm/src/command/ClockTimer/ACT_spt.c \
tpm/src/command/ClockTimer/ClockRateAdjust.c \
tpm/src/command/ClockTimer/ClockSet.c tpm/src/command/ClockTimer/ReadClock.c \
tpm/src/command/CommandAudit/SetCommandCodeAuditStatus.c \
tpm/src/command/Context/ContextLoad.c tpm/src/command/Context/ContextSave.c \
tpm/src/command/Context/Context_spt.c tpm/src/command/Context/EvictControl.c \
tpm/src/command/Context/FlushContext.c \
tpm/src/command/DA/DictionaryAttackLockReset.c \
tpm/src/command/DA/DictionaryAttackParameters.c \
tpm/src/command/Duplication/Duplicate.c tpm/src/command/Duplication/Import.c \
tpm/src/command/Duplication/Rewrap.c tpm/src/command/EA/PolicyAuthValue.c \
tpm/src/command/EA/PolicyAuthorize.c tpm/src/command/EA/PolicyAuthorizeNV.c \
tpm/src/command/EA/PolicyCommandCode.c tpm/src/command/EA/PolicyCounterTimer.c \
tpm/src/command/EA/PolicyCpHash.c tpm/src/command/EA/PolicyDuplicationSelect.c \
tpm/src/command/EA/PolicyGetDigest.c tpm/src/command/EA/PolicyLocality.c \
tpm/src/command/EA/PolicyNV.c tpm/src/command/EA/PolicyNameHash.c \
tpm/src/command/EA/PolicyNvWritten.c tpm/src/command/EA/PolicyOR.c \
tpm/src/command/EA/PolicyPCR.c tpm/src/command/EA/PolicyPassword.c \
tpm/src/command/EA/PolicyPhysicalPresence.c tpm/src/command/EA/PolicySecret.c \
tpm/src/command/EA/PolicySigned.c tpm/src/command/EA/PolicyTemplate.c \
tpm/src/command/EA/PolicyTicket.c tpm/src/command/EA/Policy_spt.c \
tpm/src/command/Ecdaa/Commit.c tpm/src/command/FieldUpgrade/FieldUpgradeData.c \
tpm/src/command/FieldUpgrade/FieldUpgradeStart.c \
tpm/src/command/FieldUpgrade/FirmwareRead.c \
tpm/src/command/HashHMAC/EventSequenceComplete.c \
tpm/src/command/HashHMAC/HMAC_Start.c \
tpm/src/command/HashHMAC/HashSequenceStart.c \
tpm/src/command/HashHMAC/MAC_Start.c \
tpm/src/command/HashHMAC/SequenceComplete.c \
tpm/src/command/HashHMAC/SequenceUpdate.c tpm/src/command/Hierarchy/ChangeEPS.c \
tpm/src/command/Hierarchy/ChangePPS.c tpm/src/command/Hierarchy/Clear.c \
tpm/src/command/Hierarchy/ClearControl.c \
tpm/src/command/Hierarchy/CreatePrimary.c \
tpm/src/command/Hierarchy/HierarchyChangeAuth.c \
tpm/src/command/Hierarchy/HierarchyControl.c \
tpm/src/command/Hierarchy/SetPrimaryPolicy.c tpm/src/command/Misc/PP_Commands.c \
tpm/src/command/Misc/SetAlgorithmSet.c tpm/src/command/NVStorage/NV_Certify.c \
tpm/src/command/NVStorage/NV_ChangeAuth.c \
tpm/src/command/NVStorage/NV_DefineSpace.c \
tpm/src/command/NVStorage/NV_Extend.c \
tpm/src/command/NVStorage/NV_GlobalWriteLock.c \
tpm/src/command/NVStorage/NV_Increment.c tpm/src/command/NVStorage/NV_Read.c \
tpm/src/command/NVStorage/NV_ReadLock.c \
tpm/src/command/NVStorage/NV_ReadPublic.c \
tpm/src/command/NVStorage/NV_SetBits.c \
tpm/src/command/NVStorage/NV_UndefineSpace.c \
tpm/src/command/NVStorage/NV_UndefineSpaceSpecial.c \
tpm/src/command/NVStorage/NV_Write.c tpm/src/command/NVStorage/NV_WriteLock.c \
tpm/src/command/NVStorage/NV_spt.c tpm/src/command/Object/ActivateCredential.c \
tpm/src/command/Object/Create.c tpm/src/command/Object/CreateLoaded.c \
tpm/src/command/Object/Load.c tpm/src/command/Object/LoadExternal.c \
tpm/src/command/Object/MakeCredential.c \
tpm/src/command/Object/ObjectChangeAuth.c tpm/src/command/Object/Object_spt.c \
tpm/src/command/Object/ReadPublic.c tpm/src/command/Object/Unseal.c \
tpm/src/command/PCR/PCR_Allocate.c tpm/src/command/PCR/PCR_Event.c \
tpm/src/command/PCR/PCR_Extend.c tpm/src/command/PCR/PCR_Read.c \
tpm/src/command/PCR/PCR_Reset.c tpm/src/command/PCR/PCR_SetAuthPolicy.c \
tpm/src/command/PCR/PCR_SetAuthValue.c tpm/src/command/Random/GetRandom.c \
tpm/src/command/Random/StirRandom.c tpm/src/command/Session/PolicyRestart.c \
tpm/src/command/Session/StartAuthSession.c tpm/src/command/Signature/Sign.c \
tpm/src/command/Signature/VerifySignature.c tpm/src/command/Startup/Shutdown.c \
tpm/src/command/Startup/Startup.c tpm/src/command/Symmetric/EncryptDecrypt.c \
tpm/src/command/Symmetric/EncryptDecrypt2.c \
tpm/src/command/Symmetric/EncryptDecrypt_spt.c tpm/src/command/Symmetric/HMAC.c \
tpm/src/command/Symmetric/Hash.c tpm/src/command/Symmetric/MAC.c \
tpm/src/command/Testing/GetTestResult.c \
tpm/src/command/Testing/IncrementalSelfTest.c \
tpm/src/command/Testing/SelfTest.c tpm/src/command/Vendor/Vendor_TCG_Test.c \
tpm/src/crypt/AlgorithmTests.c tpm/src/crypt/BnConvert.c tpm/src/crypt/BnMath.c \
tpm/src/crypt/BnMemory.c tpm/src/crypt/CryptCmac.c tpm/src/crypt/CryptDes.c \
tpm/src/crypt/CryptEccCrypt.c tpm/src/crypt/CryptEccData.c \
tpm/src/crypt/CryptEccKeyExchange.c tpm/src/crypt/CryptEccMain.c \
tpm/src/crypt/CryptEccSignature.c \
tpm/src/crypt/CryptHash.c tpm/src/crypt/CryptPrime.c \
tpm/src/crypt/CryptPrimeSieve.c tpm/src/crypt/CryptRand.c \
tpm/src/crypt/CryptRsa.c tpm/src/crypt/CryptSelfTest.c \
tpm/src/crypt/CryptSmac.c tpm/src/crypt/CryptSym.c tpm/src/crypt/CryptUtil.c \
tpm/src/crypt/PrimeData.c tpm/src/crypt/RsaKeyCache.c tpm/src/crypt/Ticket.c \
tpm/src/crypt/wolf/TpmToWolfDesSupport.c tpm/src/crypt/wolf/TpmToWolfMath.c \
tpm/src/crypt/wolf/TpmToWolfSupport.c tpm/src/events/_TPM_Hash_Data.c \
tpm/src/events/_TPM_Hash_End.c tpm/src/events/_TPM_Hash_Start.c \
tpm/src/events/_TPM_Init.c tpm/src/main/CommandDispatcher.c \
tpm/src/main/ExecCommand.c tpm/src/main/SessionProcess.c \
tpm/src/subsystem/CommandAudit.c tpm/src/subsystem/DA.c \
tpm/src/subsystem/Hierarchy.c tpm/src/subsystem/NvDynamic.c \
tpm/src/subsystem/NvReserved.c tpm/src/subsystem/Object.c \
tpm/src/subsystem/PCR.c tpm/src/subsystem/PP.c tpm/src/subsystem/Session.c \
tpm/src/subsystem/Time.c tpm/src/support/AlgorithmCap.c tpm/src/support/Bits.c \
tpm/src/support/CommandCodeAttributes.c tpm/src/support/Entity.c \
tpm/src/support/Global.c tpm/src/support/Handle.c tpm/src/support/IoBuffers.c \
tpm/src/support/Locality.c tpm/src/support/Manufacture.c \
tpm/src/support/Marshal.c tpm/src/support/MathOnByteBuffers.c \
tpm/src/support/Memory.c tpm/src/support/Power.c tpm/src/support/PropertyCap.c \
tpm/src/support/Response.c tpm/src/support/ResponseCodeProcessing.c \
tpm/src/support/TableDrivenMarshal.c tpm/src/support/TableMarshalData.c \
tpm/src/support/TpmFail.c tpm/src/support/TpmSizeChecks.c \
 ../external/wolfssl/wolfcrypt/src/aes.c ../external/wolfssl/wolfcrypt/src/arc4.c ../external/wolfssl/wolfcrypt/src/asm.c ../external/wolfssl/wolfcrypt/src/asn.c ../external/wolfssl/wolfcrypt/src/blake2b.c ../external/wolfssl/wolfcrypt/src/blake2s.c ../external/wolfssl/wolfcrypt/src/camellia.c ../external/wolfssl/wolfcrypt/src/chacha20_poly1305.c ../external/wolfssl/wolfcrypt/src/chacha.c ../external/wolfssl/wolfcrypt/src/cmac.c ../external/wolfssl/wolfcrypt/src/coding.c ../external/wolfssl/wolfcrypt/src/compress.c ../external/wolfssl/wolfcrypt/src/cpuid.c ../external/wolfssl/wolfcrypt/src/cryptocb.c ../external/wolfssl/wolfcrypt/src/curve25519.c ../external/wolfssl/wolfcrypt/src/curve448.c ../external/wolfssl/wolfcrypt/src/des3.c ../external/wolfssl/wolfcrypt/src/dh.c ../external/wolfssl/wolfcrypt/src/dsa.c ../external/wolfssl/wolfcrypt/src/ecc.c ../external/wolfssl/wolfcrypt/src/ecc_fp.c ../external/wolfssl/wolfcrypt/src/ed25519.c ../external/wolfssl/wolfcrypt/src/ed448.c ../external/wolfssl/wolfcrypt/src/error.c ../external/wolfssl/wolfcrypt/src/evp.c ../external/wolfssl/wolfcrypt/src/fe_448.c ../external/wolfssl/wolfcrypt/src/fe_low_mem.c ../external/wolfssl/wolfcrypt/src/fe_operations.c ../external/wolfssl/wolfcrypt/src/ge_448.c ../external/wolfssl/wolfcrypt/src/ge_low_mem.c ../external/wolfssl/wolfcrypt/src/ge_operations.c ../external/wolfssl/wolfcrypt/src/hash.c ../external/wolfssl/wolfcrypt/src/hc128.c ../external/wolfssl/wolfcrypt/src/hmac.c ../external/wolfssl/wolfcrypt/src/idea.c ../external/wolfssl/wolfcrypt/src/integer.c ../external/wolfssl/wolfcrypt/src/logging.c ../external/wolfssl/wolfcrypt/src/md2.c ../external/wolfssl/wolfcrypt/src/md4.c ../external/wolfssl/wolfcrypt/src/md5.c ../external/wolfssl/wolfcrypt/src/memory.c ../external/wolfssl/wolfcrypt/src/misc.c ../external/wolfssl/wolfcrypt/src/pkcs12.c ../external/wolfssl/wolfcrypt/src/pkcs7.c ../external/wolfssl/wolfcrypt/src/poly1305.c ../external/wolfssl/wolfcrypt/src/pwdbased.c ../external/wolfssl/wolfcrypt/src/rabbit.c ../external/wolfssl/wolfcrypt/src/random.c ../external/wolfssl/wolfcrypt/src/rc2.c ../external/wolfssl/wolfcrypt/src/ripemd.c ../external/wolfssl/wolfcrypt/src/rsa.c ../external/wolfssl/wolfcrypt/src/sha256.c ../external/wolfssl/wolfcrypt/src/sha3.c  ../external/wolfssl/wolfcrypt/src/sha.c ../external/wolfssl/wolfcrypt/src/signature.c ../external/wolfssl/wolfcrypt/src/sp_arm32.c ../external/wolfssl/wolfcrypt/src/sp_arm64.c ../external/wolfssl/wolfcrypt/src/sp_armthumb.c ../external/wolfssl/wolfcrypt/src/sp_c32.c ../external/wolfssl/wolfcrypt/src/sp_c64.c ../external/wolfssl/wolfcrypt/src/sp_cortexm.c ../external/wolfssl/wolfcrypt/src/sp_dsp32.c ../external/wolfssl/wolfcrypt/src/sp_int.c ../external/wolfssl/wolfcrypt/src/sp_x86_64.c ../external/wolfssl/wolfcrypt/src/srp.c ../external/wolfssl/wolfcrypt/src/tfm.c ../external/wolfssl/wolfcrypt/src/wc_dsp.c ../external/wolfssl/wolfcrypt/src/wc_encrypt.c ../external/wolfssl/wolfcrypt/src/wc_pkcs11.c ../external/wolfssl/wolfcrypt/src/wc_port.c ../external/wolfssl/wolfcrypt/src/wolfevent.c ../external/wolfssl/wolfcrypt/src/wolfmath.c \
../external/wolfssl/wolfcrypt/src/sha512.c

#tpm/src/X509/X509_ECC.c \
#tpm/src/X509/X509_RSA.c \
#tpm/src/X509/X509_spt.c \
#tpm/src/X509/X509_ECC.c \
#tpm/src/command/Attestation/CertifyCreation.c \
#tpm/src/command/Attestation/CertifyX509.c \

CFLAGS+=-DXMEMSET=memset -DXMEMCPY=memcpy
CFLAGS+=-mno-div
CFLAGS+=-DLIB_EXPORT= -DEXTERN=extern
#TPM_SRC += Clock.c Main.c tis.c CryptPqcMain.c stream.c PQC_Private.c tpm/PQC_Public.c 
VPATH += ./mstpm/external/wolfssl/wolfcrypt/src/
TPM_OBJ = $(call objs, $(TPM_SRC))
WOLF_OBJ = $(call objs, $(WOLF_SRC))
OBJ += $(TPM_OBJ) $(WOLF_OBJ)
#CFLAGS+=-DWITH_SPI 
CFLAGS+=-w
CFLAGS+=-DUSE_DA_USED=NO 
CFLAGS+=-DRADIX_BITS=32
CFLAGS+=-DTFM_NO_ASM
CFLAGS+=-DWOLFSSL_NO_ASM
CFLAGS+=-DWOLFSSL_KEY_GEN
CFLAGS+= -Imstpm/TPMCmd/tpm/include/Wolf/ -Imstpm/TPMCmd/Platform/include/ -Imstpm/TPMCmd/tpm/include/ -Imstpm/TPMCmd/Platform/include/prototypes/ -Imstpm/TPMCmd/tpm/include/prototypes/ -Imstpm/TPMCmd/test/
CFLAGS+=-DHASH_LIB=Wolf -DSYM_LIB=Wolf -DMATH_LIB=Wolf -Imstpm/external/wolfssl/ -Imstpm/TPMCmd/tpm/include/prototypes/
CFLAGS+=-DMAX_ACTIVE_SESSIONS=2 -DMAX_LOADED_SESSIONS=1 -DMAX_LOADED_OBJECTS=1 -DMAX_COMMAND_SIZE=2048 -DMAX_RESPONSE_SIZE=2048 -DNV_MEMORY_SIZE=4096 
CFLAGS+=-DWC_NO_HARDEN
CFLAGS+=-DNO_WOLFSSL_DIR
CFLAGS+=-DNO_WRITEV
CFLAGS+=-DNO_STDIO_FILESYSTEM
CFLAGS+=-DHAVE_ECC
CFLAGS+=-DECC_SHAMIR
#ecc.c.o: CFLAGS+=-DECC_SHAMIR -DHAVE_ECC
#integer.c.o:  CFLAGS+=-DECC_SHAMIR -DHAVE_ECC
#tfm.c.o: CFLAGS+=-DECC_SHAMIR -DHAVE_ECC
#sha512.c.o: CFLAGS+=-DWOLFSSL_SHA384
CFLAGS+=-fstack-usage
CFLAGS+=-DHAVE_TIME_T_TYPE
CFLAGS+=-DWC_NO_CACHE_RESISTANT
#CFLAGS+=-DTIME_OVERRIDES
CFLAGS+=-DWOLF_SHA384
CFLAGS+=-DWOLFSSL_SHA384
#CFLAGS+=-DLITTLE_ENDIAN_ORDER
CFLAGS+=-DWOLFSSL_USER_SETTINGS
#CFLAGS+=-DUSE_FAST_MATH
#CFLAGS+=-DHAVE_CONFIG
#CFLAGS+=-DHAVE_FIPS

#CFLAGS+=-DECC_SHAMIR
CFLAGS+=-DWOLFSSL_AES_NO_UNROLL
#CFLAGS+=-DLIB_EXPORT= -include stdint.h -DINT32=int32_t -DBYTE=uint8_t
CFLAGS+=-DSELF_TEST=NO -DLIBRARY_COMPATIBILITY_CHECK=NO
#CFLAGS+=-DHAVE_WOLF_BIGINT
CFLAGS+= -DUSE_FAST_MATH
CFLAGS += -DSINGLE_THREADED       
CFLAGS += -DNO_WOLFSSL_CLIENT     
CFLAGS += -DNO_WOLFSSL_SERVER     
#CFLAGS += -DOPENSSL_EXTRA         
CFLAGS += -DNO_FILESYSTEM         
CFLAGS += -DWOLFSSL_AES_DIRECT
CFLAGS += -DAES_128=1
CFLAGS += -DAES_192=0
CFLAGS += -DAES_256=0
#CFLAGS += -DWOLFSSL_USER_SETTINGS 
CFLAGS += -DSTRING_USER           
#CFLAGS += -DCTYPE_USER            
CFLAGS += -DCERTIFYX509_DEBUG=NO  -DCC_Certify=CC_NO -DCC_CertifyX509=CC_NO -DCC_CertifyCreation=CC_NO
#CFLAGS += -DALG_SHA512=ALG_NO -DALG_SHA_384=ALG_NO
CFLAGS += -DALG_ECC=ALG_YES
CFLAGS += -DALG_RSA=ALG_YES
CFLAGS += -DRSA_KEY_SIEVE=NO
CFLAGS += -DTABLE_DRIVEN_DISPATCH=NO
# -DALG_ECDH=ALG_NO -DALG_ECDSA=ALG_NO
CFLAGS +=	-Wno-unused-function    
CFLAGS +=	-DNO_INLINE
CFLAGS += -DWOLFSSL_NO_MEMORY


CFLAGS+=-DCC_ACT_SetTimeout=CC_NO
CFLAGS+=-DCC_AC_GetCapability=CC_NO
CFLAGS+=-DCC_AC_Send=CC_NO
CFLAGS+=-DCC_ActivateCredential=CC_NO
CFLAGS+=-DCC_Certify=CC_NO
CFLAGS+=-DCC_CertifyCreation=CC_NO
CFLAGS+=-DCC_CertifyX509=CC_NO
CFLAGS+=-DCC_ChangeEPS=CC_NO
CFLAGS+=-DCC_ChangePPS=CC_NO
CFLAGS+=-DCC_Clear=CC_NO
CFLAGS+=-DCC_ClearControl=CC_NO
CFLAGS+=-DCC_ClockRateAdjust=CC_NO
CFLAGS+=-DCC_ClockSet=CC_NO
CFLAGS+=-DCC_Commit=CC_NO
CFLAGS+=-DCC_ContextLoad=CC_NO
CFLAGS+=-DCC_ContextSave=CC_NO
#CFLAGS+=-DCC_Create=CC_NO
#CFLAGS+=-DCC_CreateLoaded=CC_NO
#CFLAGS+=-DCC_CreatePrimary=CC_NO
CFLAGS+=-DCC_DictionaryAttackLockReset=CC_NO
CFLAGS+=-DCC_DictionaryAttackParameters=CC_NO
CFLAGS+=-DCC_Duplicate=CC_NO
#CFLAGS+=-DCC_ECC_Decrypt=CC_NO
#CFLAGS+=-DCC_ECC_Encrypt=CC_NO
CFLAGS+=-DCC_ECC_Parameters=CC_NO
CFLAGS+=-DCC_ECDH_KeyGen=CC_NO
CFLAGS+=-DCC_ECDH_ZGen=CC_NO
CFLAGS+=-DCC_EC_Ephemeral=CC_NO
CFLAGS+=-DCC_EncryptDecrypt=CC_NO
CFLAGS+=-DCC_EncryptDecrypt2=CC_NO
CFLAGS+=-DCC_EventSequenceComplete=CC_NO
CFLAGS+=-DCC_EvictControl=CC_NO
CFLAGS+=-DCC_FlushContext=CC_NO
CFLAGS+=-DCC_GetCapability=CC_NO
CFLAGS+=-DCC_GetCommandAuditDigest=CC_NO
#CFLAGS+=-DCC_GetRandom=CC_NO
CFLAGS+=-DCC_GetSessionAuditDigest=CC_NO
CFLAGS+=-DCC_GetTestResult=CC_NO
CFLAGS+=-DCC_GetTime=CC_NO
#CFLAGS+=-DCC_HMAC=CC_NO
#CFLAGS+=-DCC_HMAC_Start=CC_NO
#CFLAGS+=-DCC_Hash=CC_NO
#CFLAGS+=-DCC_HashSequenceStart=CC_NO
CFLAGS+=-DCC_HierarchyChangeAuth=CC_NO
CFLAGS+=-DCC_HierarchyControl=CC_NO
CFLAGS+=-DCC_Import=CC_NO
CFLAGS+=-DCC_IncrementalSelfTest=CC_NO
#CFLAGS+=-DCC_Load=CC_NO
#CFLAGS+=-DCC_LoadExternal=CC_NO
#CFLAGS+=-DCC_MAC=CC_NO
#CFLAGS+=-DCC_MAC_Start=CC_NO
CFLAGS+=-DCC_MakeCredential=CC_NO
CFLAGS+=-DCC_NV_Certify=CC_NO
CFLAGS+=-DCC_NV_ChangeAuth=CC_NO
CFLAGS+=-DCC_NV_DefineSpace=CC_NO
CFLAGS+=-DCC_NV_Extend=CC_NO
CFLAGS+=-DCC_NV_GlobalWriteLock=CC_NO
CFLAGS+=-DCC_NV_Increment=CC_NO
CFLAGS+=-DCC_NV_Read=CC_NO
CFLAGS+=-DCC_NV_ReadLock=CC_NO
CFLAGS+=-DCC_NV_ReadPublic=CC_NO
CFLAGS+=-DCC_NV_SetBits=CC_NO
CFLAGS+=-DCC_NV_UndefineSpace=CC_NO
CFLAGS+=-DCC_NV_UndefineSpaceSpecial=CC_NO
CFLAGS+=-DCC_NV_Write=CC_NO
CFLAGS+=-DCC_NV_WriteLock=CC_NO
CFLAGS+=-DCC_ObjectChangeAuth=CC_NO
CFLAGS+=-DCC_PCR_Allocate=CC_NO
CFLAGS+=-DCC_PCR_Event=CC_NO
CFLAGS+=-DCC_PCR_Extend=CC_NO
CFLAGS+=-DCC_PCR_Read=CC_NO
CFLAGS+=-DCC_PCR_Reset=CC_NO
CFLAGS+=-DCC_PCR_SetAuthPolicy=CC_NO
CFLAGS+=-DCC_PCR_SetAuthValue=CC_NO
CFLAGS+=-DCC_PP_Commands=CC_NO
CFLAGS+=-DCC_PolicyAuthValue=CC_NO
CFLAGS+=-DCC_PolicyAuthorize=CC_NO
CFLAGS+=-DCC_PolicyAuthorizeNV=CC_NO
CFLAGS+=-DCC_PolicyCommandCode=CC_NO
CFLAGS+=-DCC_PolicyCounterTimer=CC_NO
CFLAGS+=-DCC_PolicyCpHash=CC_NO
CFLAGS+=-DCC_PolicyDuplicationSelect=CC_NO
CFLAGS+=-DCC_PolicyGetDigest=CC_NO
CFLAGS+=-DCC_PolicyLocality=CC_NO
CFLAGS+=-DCC_PolicyNV=CC_NO
CFLAGS+=-DCC_PolicyNameHash=CC_NO
CFLAGS+=-DCC_PolicyNvWritten=CC_NO
CFLAGS+=-DCC_PolicyOR=CC_NO
CFLAGS+=-DCC_PolicyPCR=CC_NO
CFLAGS+=-DCC_PolicyPassword=CC_NO
CFLAGS+=-DCC_PolicyPhysicalPresence=CC_NO
CFLAGS+=-DCC_PolicyRestart=CC_NO
CFLAGS+=-DCC_PolicySecret=CC_NO
CFLAGS+=-DCC_PolicySigned=CC_NO
CFLAGS+=-DCC_PolicyTemplate=CC_NO
CFLAGS+=-DCC_PolicyTicket=CC_NO
CFLAGS+=-DCC_Policy_AC_SendSelect=CC_NO
CFLAGS+=-DCC_Quote=CC_NO
CFLAGS+=-DCC_RSA_Decrypt=CC_NO
CFLAGS+=-DCC_RSA_Encrypt=CC_NO
CFLAGS+=-DCC_ReadClock=CC_NO
CFLAGS+=-DCC_ReadPublic=CC_NO
CFLAGS+=-DCC_Rewrap=CC_NO
CFLAGS+=-DCC_SelfTest=CC_NO
CFLAGS+=-DCC_SequenceComplete=CC_NO
CFLAGS+=-DCC_SequenceUpdate=CC_NO
CFLAGS+=-DCC_SetAlgorithmSet=CC_NO
CFLAGS+=-DCC_SetCommandCodeAuditStatus=CC_NO
CFLAGS+=-DCC_SetPrimaryPolicy=CC_NO
#CFLAGS+=-DCC_Shutdown=CC_NO
CFLAGS+=-DCC_Sign=CC_NO
#CFLAGS+=-DCC_StartAuthSession=CC_NO
#CFLAGS+=-DCC_Startup=CC_NO
CFLAGS+=-DCC_StirRandom=CC_NO
CFLAGS+=-DCC_TestParms=CC_NO
CFLAGS+=-DCC_Unseal=CC_NO
CFLAGS+=-DCC_Vendor_TCG_Test=CC_NO
CFLAGS+=-DCC_VerifySignature=CC_NO
CFLAGS+=-DCC_ZGen_2Phase=CC_NO

