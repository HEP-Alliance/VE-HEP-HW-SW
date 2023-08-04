

buildECP5:
    mkdir rtl/gen || true
    ./gradlew runHSMECP5
    yosys -q -l out/PQVexRiscvECP5_yosys.log -p "synth_ecp5 -json out/PQVexRiscvECP5.json -top PQVexRiscvVEHEP" ./out/PQVexRiscvVEHEP.v ./src/main/resources/AES_Masked.v -DFUNCTIONAL
    nextpnr-ecp5 -l out/PQVexRiscvUP5K_pnr.log --um5g-85k --package CABGA381 --freq 12 --json out/PQVexRiscvECP5.json --lpf src/main/resources/ecp5evn.lpf --textcfg out/PQVexRiscvECP5.config --lpf-allow-unconstrained
    ecppack --db  $(which ecppack | sed -e 's#bin/ecppack##')share/trellis/database --svf-rowsize 100000 --svf out/PQVexRiscvECP5.svf out/PQVexRiscvECP5.config out/PQVexRiscvECP5.bit

run:
  gdb -ex "set confirm off" -ex "set pagnination off" -ex "set remote-timeout 10" -ex "target extended-remote :3333" -ex load -ex run out/main
