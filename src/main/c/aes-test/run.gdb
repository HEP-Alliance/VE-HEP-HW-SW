set remotetimeout 10
set pagination off
target extended-remote | openocd-vexriscv -c "gdb_port pipe; log_output openocd.log" -f hsmsim.cfg
load
monitor reset
run
