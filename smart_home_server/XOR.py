import random
import base64
import bitarray
import binascii

def create_key(key_length):
    key = ""

    for _ in range(key_length):
        key = key + str(random.randint(0, 1))

    return key

def text_to_bits(text):
    res = bin(int.from_bytes(text, 'big'))[2:]
    res = "0" + res
    return res

def bits_to_text(bits):
    bits = "0b" + bits
    n = int(bits, 2)
    return n.to_bytes((n.bit_length() + 7) // 8, 'big')














