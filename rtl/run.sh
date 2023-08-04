rm PQVexRiscvECP5.*
cd ..
./gradlew runHSMECP5 || exit 1
cd -
yosys  -l PQVexRiscvUP5K_yosys.log -p "synth_ecp5 -json PQVexRiscvECP5.json -top PQVexRiscvECP5" ../out/PQVexRiscvECP5.v
nextpnr-ecp5 -l PQVexRiscvUP5K_pnr.log --um5g-85k --package CABGA381  --json PQVexRiscvECP5.json --lpf ecp5evn.lpf --textcfg PQVexRiscvECP5.config
#ecppack --db /nix/store/gmdv74zzd2k75njchxyyk3mzz5gk2dcd-trellis-2021-09-01/share/trellis/database --svf-rowsize 100000 --svf PQVexRiscvECP5.svf PQVexRiscvECP5.config PQVexRiscvECP5.bit 
ecppack --db /nix/store/gmdv74zzd2k75njchxyyk3mzz5gk2dcd-trellis-2021-09-01/share/trellis/database --svf PQVexRiscvECP5.svf PQVexRiscvECP5.config PQVexRiscvECP5.bit 
#ecppack --db /nix/store/mf3biqipbjaamhv8zhan5rbcl1vxfz8i-trellis-2021-09-01/share/trellis/database --svf PQVexRiscvECP5.svf PQVexRiscvECP5.config PQVexRiscvECP5.bit 
openocd -f ecp5-evn.cfg -c "transport select jtag; init; svf ./PQVexRiscvECP5.svf; exit"
gdb ../out/main.elf --command run.gdb
