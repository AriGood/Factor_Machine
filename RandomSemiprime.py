import random
import time
import sys

#get a prime number 
def gen_random_prime(bits):
    while True:
        n = random.randrange(1 << (bits - 1), 1 << bits)
        if is_prime(n):
            return n

# Fermat primality test far faster than normal algrithim 
def is_prime(n, trials = 128):
    for i in range(trials):
        if pow(random.randint(2, n - 2), n - 1, n) != 1:
            return False
    return True

# def is_prime(n: int) -> bool:
#     if n <= 3:
#         return n > 1
#     if n % 2 == 0 or n % 3 == 0:
#         return False
#     limit = int(n**0.5)
#     for i in range(5, limit+1, 6):
#         if n % i == 0 or n % (i+2) == 0:
#             return False
#     return True

def Random_Semiprime(bits):
    try:
        if (bits>212):
            print("range error")
        else:
            print(gen_random_prime(bits // 2) * gen_random_prime(bits // 2))
    except:
        print(15)

Random_Semiprime(int(sys.argv[1]))
