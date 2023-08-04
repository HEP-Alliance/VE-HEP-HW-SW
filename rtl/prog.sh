openocd -f ecp5-evn.cfg -c "transport select jtag; init; svf ./PQVexRiscvECP5.svf; exit"
