DEVICE=up5k

cd ..
./gradlew runHSMUP5K
cd -
yosys -q -l PQVexRiscvUP5K_yosys.log -p "synth_ice40 -top PQVexRiscvUP5K -json PQVexRiscvUP5K.json -dsp" ../out/PQVexRiscvUP5K.v
nextpnr-ice40 --force -l PQVexRiscvUP5K_pnr.log --up5k --package sg48 --json PQVexRiscvUP5K.json --pcf PQVexRiscvUP5K.pcf --asc PQVexRiscvUP5K.asc
icepack PQVexRiscvUP5K.asc PQVexRiscvUP5K.bit
