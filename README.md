# VE-HEP

This is the VE-HEP HSM repository. If you want to run the simulation
on your pc you can do it like proposed in the following example. Further 
down you find instruction on how to generate a bitstream for the FPGA
and how to build and program the TPM2.0 Firmware for the HSM. For operating
the TPM2.0 Firmware we would refer you to the TPM2.0 [Specification](https://trustedcomputinggroup.org/resource/tpm-library-specification/).


### Setup

`nix-shell` or for direnv users, `direnv allow`.

Non-Nix users, please note that a custom `symbiosys` version is used. Also maybe
the Java and Scala versions are important.

IDE users can generate a project with `./gradlew eclipse` or `./gradlew idea`,
respectively.

For more details view this ![README.md](https://github.com/VE-HEP/riscv-nix#readme).


### Running Code in the Simulation

Example hello world with C in simulation:
```
make -C src/main/c/hello_world all
./gradlew runHSMSim
```


### FPGA bitstream generation

We used the [ECP5 Evaluation Board](https://www.latticesemi.com/products/developmentboardsandkits/ecp5evaluationboard) for development but other ECP5 FPGA boards might work as well.

The ECP5 fpga bitstream for the design can be generated using the follwing command:

```
just buildECP5
```

### Programming the FPGA 
The following command can be used to programm the FPGA:
```
openocd -f rtl/ecp5-evn.cfg -c "transport select jtag; init; svf out/PQVexRiscvECP5.svf; exit"
```
this wil generate a .svf and a .bit file that can be placed onto the Lattice ECP5 FPGA using either ecp5prog (to place it in the spi flash memory of the board) or openocd (to directly place the bitstream on the FPGA SRAM).


### Wiring the FPGA board

| Function | Location |
| -------- | -------- |
|uart_txd  |   B15    |
|uart_rxd  |   C15    |
|jtag_tdo  |   B20    |
|jtag_tdi  |   E11    |
|jtag_tck  |   C12    |
|jtag_tms  |   E12    |
|spi_sclk  |   B13    |
|spi_mosi  |   D11    |
|spi_miso_write | B12   |
|spi_ss    |   D12    |


# Formal verification with SpinalHDL

Tool stack:

- SpinalHDL -> Verilog hardware generation + formal statements
- SymbiYosys as Yosys frontend + runner for formal verification
- Scala as programming language, Gradle as build system
- (optional) Nix for a reproducible working environment, direnv for comfort

## Usage

At the moment, there are no individual run tasks, only test.

Run a specific test:

```bash
# See https://github.com/maiflai/gradle-scalatest for more CLI options
./gradlew test --tests MyTest
```

Every test will create a folder in `./out` and put synthesized Verilog files and
SymbiYosys working directories in there.

## VEHEP Simulation 

In order to test the design with a firmware one needs to compile the firmware
using the GNU make build system:

```
make -C src/main/c/hello_world/ 
```

this automatically places a `main.bin` in `./out/`

The simulation can be launched using the folowing command:
```
./gradlew runHSMSim
```

Example output:

```
$ ./gradlew runHSMSim

> Task :runHSMSim
[Runtime] SpinalHDL v1.6.1    git head : 7e3c3d1367eaac655f4ad5636fba441852fe073e
[Runtime] JVM max memory : 3920.0MiB
[Runtime] Current date : 2023.07.05 16:16:34
[Progress] at 0.000 : Elaborate components
[Progress] at 1.174 : Checks and transforms
[Progress] at 1.543 : Generate Verilog
[Warning] 123 signals were pruned. You can call printPruned on the backend report to get more informations.
[Done] at 2.362
[Progress] Simulation workspace in /tmp/ap5/./simWorkspace/PQVexRiscvSim
[Progress] Verilator compilation started
[Progress] Verilator compilation done in 5683.829 ms
[Progress] Start PQVexRiscvSim PqVexRiscvSim simulation with seed 42
Simulating mupq.PQVexRiscvSim with JtagTcp on port 7894
WAITING FOR TCP JTAG CONNECTION
WAITING FOR TCP Spi CONNECTION
1. Hello World!
2. Hello World!
3. Hello World!
```

it is also possible to load compiled .elf binaries and debug them  on 
the simulated HSM using the tcpjtag connection with openocd/gdb. 

### TODOs here 
- add example for jtag connection loading the hello world binary with gdb

## HSM Firmware

The TPM2.0 (basically the reference implementation by Microsoft) can be build using the following commands:

```
make -C src/main/c/hsm fetch
make -C src/main/c/hsm all
```
Similar to the hello_world example this will place a main.{bin,elf} in the out/ directory.  






## Examples

- `SlowdownTest` formally verifies Spinal's `Stream#slowdown` method (actually,
  it's an equivalent re-implementation in order to get better Verilog output and
  access interna).
- `SATSolver` synthesizes a simple number factorization problem to assess the
  performance overhead of the hardware tooling. (Result: It is about an order
  of magnitude slower than the `factorize` command, which probably is pretty
  good actually. Also, there is no limit on the bit depth of the problem, the
  `depth` parameter controls the *time* depth, i.e. the number of cycles.)

## Project structure

The project follows the standard Gradle project architecture:
`./src/{main,test}/{c,java,scala,verilog,resources}/` directories for sources,
`./out` for build artifacts and top-level for configuration.
