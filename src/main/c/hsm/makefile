PROJ_NAME=hsm
DEBUG=yes
BENCH=no
MULDIV=no

SRCS = 	$(wildcard src/*.c) \
		$(wildcard src/libnosys/*.c)\
		$(wildcard src/*.cpp)  \
		$(wildcard src/*.S)



OBJDIR = build

INC  =
LIBS =
LIBSINC = -L$(OBJDIR)
LDSCRIPT = ./src/linker.ld

#include ../../../resources/gcc.mk
# Set it to yes if you are using the sifive precompiled GCC pack
RISCV_NAME ?= riscv32-unknown-elf
#ifeq ($(MULDIV),yes)
#	RISCV_PATH ?= /opt/riscv32im/
#else
RISCV_PATH ?= /opt/riscv32i/
#endif

MABI=ilp32
#MARCH := rv32im
MARCH := rv32im

CFLAGS += -mno-div -march=$(MARCH)  -mabi=$(MABI) -mno-div -DNDEBUG 
LDFLAGS += --specs=nosys.specs -march=$(MARCH)  -mabi=$(MABI) -L$(RISCV_PATH)

ifeq ($(DEBUG),yes)
	CFLAGS += -g3 -Os
endif

ifeq ($(DEBUG),no)
	CFLAGS += -g -Os
endif

RISCV_OBJCOPY = $(RISCV_NAME)-objcopy
RISCV_OBJDUMP = $(RISCV_NAME)-objdump
RISCV_CC=$(RISCV_NAME)-gcc

CFLAGS +=-MD -fstrict-volatile-bitfields -fno-strict-aliasing -ffunction-sections  -fdata-sections
LDFLAGS += -lgcc -mcmodel=medany -nostartfiles -ffreestanding -Wl,-Bstatic,-T,$(LDSCRIPT),-Map,$(OBJDIR)/$(PROJ_NAME).map,--print-memory-usage -Wl,--gc-sections
#LDFLAGS += -lgcc -lc -lg -nostdlib  -lgcc  -msave-restore    --strip-debug,

include ./tpm.mk
#include ./mbedtls.mk

OBJS := $(SRCS)
OBJS += $(TPM_SRCS)
OBJS := $(OBJS:.c=.o)
OBJS := $(OBJS:.cpp=.o)
OBJS := $(OBJS:.S=.o)
OBJS := $(OBJS:..=miaou)
OBJS := $(addprefix $(OBJDIR)/,$(OBJS))



fetch:
	git clone --recursive https://github.com/ben01100001/ms-tpm-20-ref.git mstpm || true

all: $(OBJDIR)/$(PROJ_NAME).elf $(OBJDIR)/$(PROJ_NAME).bin 
	mkdir -p ../../../../out
	cp $(OBJDIR)/$(PROJ_NAME).elf ../../../../out/main.elf
	cp $(OBJDIR)/$(PROJ_NAME).bin ../../../../out/main.bin

$(OBJDIR)/%.elf: $(OBJS) | $(OBJDIR)
	$(RISCV_CC) $(CFLAGS) -o $@ $^ $(LDFLAGS) $(LIBSINC) $(LIBS)

$(OBJDIR)/$(PROJ_NAME).elf: $(OBJ) 

%.hex: %.elf
	$(RISCV_OBJCOPY) -O ihex $^ $@

%.bin: %.elf
	$(RISCV_OBJCOPY) -O binary $^ $@

%.v: %.elf
	$(RISCV_OBJCOPY) -O verilog $^ $@

%.asm: %.elf
	$(RISCV_OBJDUMP) -S -d $^ > $@

$(OBJDIR)/%.o: %.c
	mkdir -p $(dir $@)
	$(RISCV_CC) -c $(CFLAGS)  $(INC) -o $@ $^
	$(RISCV_CC) -S $(CFLAGS)  $(INC) -o $@.disasm $^

$(OBJDIR)/%.o: %.cpp
	mkdir -p $(dir $@)
	$(RISCV_CC) -c $(CFLAGS)  $(INC) -o $@ $^

$(OBJDIR)/%.o: %.S
	mkdir -p $(dir $@)
	$(RISCV_CC) -c $(CFLAGS) -o $@ $^ -D__ASSEMBLY__=1

$(OBJDIR):
	mkdir -p $@


.PHONY: clean
clean:
	rm -rf $(OBJDIR)/src
	rm -f $(OBJDIR)/$(PROJ_NAME).elf
	rm -f $(OBJDIR)/$(PROJ_NAME).hex
	rm -f $(OBJDIR)/$(PROJ_NAME).map
	rm -f $(OBJDIR)/$(PROJ_NAME).v
	rm -f $(OBJDIR)/$(PROJ_NAME).asm
	find $(OBJDIR) -type f -name '*.o' -print0 | xargs -0 -r rm
	find $(OBJDIR) -type f -name '*.d' -print0 | xargs -0 -r rm
	rm -rf $(OBJDIR)/
	rm -rf external

clean-all : clean 

.SECONDARY: $(OBJS)
