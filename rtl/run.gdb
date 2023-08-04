set remotetimeout 60
set pagination off
target extended-remote :3333
monitor reset halt
set *(char*)0xF0010000=104
set *(char*)0xF0010000=101
set *(char*)0xF0010000=108
set *(char*)0xF0010000=108
set *(char*)0xF0010000=111
set *(char*)0xF0010000=32
set *(char*)0xF0010000=119
set *(char*)0xF0010000=111
set *(char*)0xF0010000=114
set *(char*)0xF0010000=108
set *(char*)0xF0010000=100
set *(char*)0xF0010000=32
set *(char*)0xF0010000=102
set *(char*)0xF0010000=114
set *(char*)0xF0010000=111
set *(char*)0xF0010000=109
set *(char*)0xF0010000=32
set *(char*)0xF0010000=106
set *(char*)0xF0010000=116
set *(char*)0xF0010000=97
set *(char*)0xF0010000=103
set *(char*)0xF0010000=10
quit
