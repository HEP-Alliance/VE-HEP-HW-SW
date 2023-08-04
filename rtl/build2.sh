#!/usr/bin/env bash
set -e -u -o pipefail
(cd .. && ./gradlew runHSMECP5 && cd -) || exit
yosys -q -l PQVexRiscvUP5K_yosys.log -p "synth_ecp5 -json PQVexRiscvECP5.json -top PQVexRiscvECP5" ../out/PQVexRiscvECP5.v
nextpnr-ecp5 -l PQVexRiscvUP5K_pnr.log --um5g-85k --package CABGA381 --freq 12 --json PQVexRiscvECP5.json --lpf ecp5evn.lpf --textcfg PQVexRiscvECP5.config
ecppack --db /nix/store/gmdv74zzd2k75njchxyyk3mzz5gk2dcd-trellis-2021-09-01/share/trellis/database --svf-rowsize 100000 --svf PQVexRiscvECP5.svf PQVexRiscvECP5.config PQVexRiscvECP5.bit 
scp PQVexRiscvECP5.svf PQVexRiscvECP5.bit lab:
ssh lab bash prog.sh
openocd -f ./prjtrellis/misc/openocd/ecp5-evn.cfg -c "transport select jtag; init; svf ./PQVexRiscvECP5.svf; exit"
gdb ../out/main --command run.gdb
