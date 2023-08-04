from Crypto.Cipher import AES
import os

def fmt(x):
    return "".join(["\\x{}".format(hex(i)[2:]) for i in list(x)])

def print_vec(k,p,c):
    assert(len(k) == len(p) == len(c) == 16)
    print("vector[{}]=(tv){{.key=\"{}\",.plain=\"{}\",.cipher=\"{}\"}};".format(i,fmt(k),fmt(p),fmt(c)))

def gen_vec():
    k,p = os.urandom(16),os.urandom(16)
    c = AES.new(k,AES.MODE_ECB).encrypt(p)
    return k,p,c


for i in range(100):
    k,p,c = gen_vec()
    print_vec(k,p,c)
