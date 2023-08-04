#!/usr/bin/env bash
        #set pagination off
gdb -silent -ex "set pagination off" -ex "target extended-remote :3333" -ex "load" -ex "run"  build/hsm.elf
