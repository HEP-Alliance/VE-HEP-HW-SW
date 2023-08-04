#!/usr/bin/env bash
cnt=`echo $1 | xxd -r -ps | wc -c`

echo $1 | xxd -r -ps | nc localhost 7895 | head -c $cnt | xxd -ps
