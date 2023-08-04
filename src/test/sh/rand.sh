echo 0bd40024 80010000000c0000017b000f | xxd -r -ps | nc localhost 7895 | head -c 16 | xxd -ps
echo 00d40018 ff | xxd -r -ps | nc localhost 7895 | head -c 4  | xxd -ps
sleep 15

echo 9bd4002400000000000000000000000000000000000000000000000000000000 | xxd -r -ps | nc localhost 7895 | head -c  32 |  xxd -ps
#for i in range 1 2 3 4 5 6 7 8 9 0; do
#    echo 8400002400000000 | xxd -r -ps | nc localhost 7895 | head -c 8 | tail -c 4 | xxd -ps 
# done



