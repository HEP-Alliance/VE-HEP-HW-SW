PROJ_NAME=hello_world
DEBUG=yes
BENCH=no
MULDIV=no

SRCS = 	$(wildcard src/*.c) \
		$(wildcard src/*.cpp)  \
		$(wildcard src/*.S)

OBJDIR = build

INC  =
LIBS =
LIBSINC = -L$(OBJDIR)
LDSCRIPT = $(OBJDIR)/link.ld


VEXRISCV_ROMSIZE=16K
VEXRISCV_RAMSIZE=16K
VEXRISCV_RWMTVEC=1
VEXRISCV_NONVOLATILE_ROM=0
CPPFLAGS=-DROMSIZE=16K -DRAMSIZE=16K -DRAMORIGIN=0x80000000


#include ../../../resources/gcc.mk
# Set it to yes if you are using the sifive precompiled GCC pack
SIFIVE_GCC_PACK ?= yes

ifeq ($(SIFIVE_GCC_PACK),yes)
	#RISCV_NAME ?= riscv64-unknown-elf
	RISCV_NAME ?= riscv32-none-elf
	RISCV_PATH ?= 
else
	RISCV_NAME ?= riscv32-unknown-elf
	ifeq ($(MULDIV),yes)
		RISCV_PATH ?= /opt/riscv32im/
	else
		RISCV_PATH ?= /opt/riscv32i/
	endif
endif

MABI=ilp32
MARCH := rv32i
ifeq ($(MULDIV),yes)
	MARCH := $(MARCH)m
endif

CFLAGS += -march=$(MARCH)  -mabi=$(MABI) -DNDEBUG 
LDFLAGS += -march=$(MARCH)  -mabi=$(MABI) -lgcc



#include ../../../resources/subproject.mk


ifeq ($(DEBUG),yes)
	CFLAGS += -g3 -O2
endif

ifeq ($(DEBUG),no)
	CFLAGS += -g -Os -flto
endif

ifeq ($(BENCH),yes)
	CFLAGS += -fno-inline
endif

ifeq ($(SIFIVE_GCC_PACK),yes)
	RISCV_CLIB=$(RISCV_PATH)/$(RISCV_NAME)/lib/$(MARCH)/$(MABI)/
else
	RISCV_CLIB=$(RISCV_PATH)/$(RISCV_NAME)/lib/
endif





RISCV_OBJCOPY = $(RISCV_NAME)-objcopy
RISCV_OBJDUMP = $(RISCV_NAME)-objdump
RISCV_CC=$(RISCV_NAME)-gcc

CFLAGS +=  -MD -fstrict-volatile-bitfields -fno-strict-aliasing
LDFLAGS +=  -nostdlib -lg -mcmodel=medany -nostartfiles -ffreestanding -Wl,-Bstatic,-T,$(LDSCRIPT),-Map,$(OBJDIR)/$(PROJ_NAME).map,--print-memory-usage -flto
#LDFLAGS += -lgcc -lc -lg -nostdlib  -lgcc  -msave-restore    --strip-debug,

OBJS := $(SRCS)
OBJS := $(OBJS:.c=.o)
OBJS := $(OBJS:.cpp=.o)
OBJS := $(OBJS:.S=.o)
OBJS := $(OBJS:..=miaou)
OBJS := $(addprefix $(OBJDIR)/,$(OBJS))


all: $(LDSCRIPT) $(OBJDIR)/$(PROJ_NAME).elf $(OBJDIR)/$(PROJ_NAME).bin $(OBJDIR)/$(PROJ_NAME).asm $(OBJDIR)/$(PROJ_NAME).v
	cp $(OBJDIR)/$(PROJ_NAME).bin ../../../../out/main.bin
	cp $(OBJDIR)/$(PROJ_NAME).elf ../../../../out/main.elf
	cp $(OBJDIR)/$(PROJ_NAME).elf ../../../../out/main
	#gdb  $(OBJDIR)/$(PROJ_NAME).elf -ex "set pagination=off" -ex "set remotetimeout=10" -ex "target extended-remote :3333" -ex "load" -ex "b itoa_test" -ex "run" 

$(OBJDIR)/%.elf: $(OBJS) | $(OBJDIR)
	$(RISCV_CC) $(CFLAGS) -o $@ $^ $(LDFLAGS) $(LIBSINC) $(LIBS)

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

$(LDSCRIPT): ./src/vexriscv.ld $(OBJDIR)
	riscv32-none-elf-cpp $(CPPFLAGS) ./src/vexriscv.ld | grep -vE "^#" > $(LDSCRIPT)

clean-all : clean 

.SECONDARY: $(OBJS)
