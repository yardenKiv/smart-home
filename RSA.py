import math
import random

MAX_P_VALUE = 20
MIN_P_VALUE = 10

MAX_Q_VALUE = 20
MIN_Q_VALUE = 10

print("a")

def create_rsa_keys():
    
    p = generate_prime(MIN_P_VALUE, MAX_P_VALUE)
    q = generate_prime(MIN_Q_VALUE, MAX_Q_VALUE)

    while p == q:
        q = generate_prime(MIN_Q_VALUE, MAX_Q_VALUE)

    print("q", q)
    print("p", p)
    n = p * q
    z = (p - 1) * (q - 1)

    e = create_e(n, z)
    d = create_d(e, z)

    private_key = {"d": d, "n": n}
    public_key = {"e": e, "n": n}

    return private_key, public_key

def encrypt_rsa(public_key, m):



    e = public_key["e"]
    n = public_key["n"]

    return m**e % n

def decrypt_rsa(private_key, c):
    d = private_key["d"]
    n = private_key["n"]

    text = c**d % n

    return text

def encrypt_text(public_key, text):

    arr = list()

    for letter in text:
        arr.append(str(encrypt_rsa(public_key, ord(letter))))

    return " ".join(arr)

def decrypt_text(private_key, encrypted_text):
    arr = encrypted_text.split(" ")
    ans = list()

    for letter in arr:
        ans.append(chr(decrypt_rsa(private_key, int(letter))))

    return "".join(ans)

def create_e(n, z):
    e = generate_prime(1, z)

    while not is_prime(e) or not is_coprime(n, e):
        e = generate_prime(1, z)

    return e

def create_d(e, z):
    d = generate_prime(1, 1000)

    while (d * e) % z != 1:
        d = generate_prime(1, 1000)

    return d


def generate_prime(min, max):
    num = 0

    while not is_prime(num):
        num = random.randint(min, max)

    return num

def is_prime(n):

    if n < 3:
        return False

    for i in range(2, int(math.sqrt(n)+1)):
        if n % i == 0:
            return False;
    return n>1;

def gcd(p, q):
    while q != 0:
        p, q = q, p%q
    return p

def is_coprime(x, y):
    return gcd(x, y) == 1




