import sys
import math
import time

start_time = time.time()
prime=[]
Semiprime = int(sys.argv[1])


def get_primes(n):
    numbers = set(range(n, 1, -1))
    primes = []
    while numbers:
        p = numbers.pop()
        primes.append(p)
        numbers.difference_update(set(range(p*2, n+1, p)))
    return primes

Sqrt=(round(math.sqrt(Semiprime)))

prime=(get_primes(Sqrt))

def factor(n,prime):
    i=1
    while True:
        if n%prime[len(prime)-i] == 0:
            return (prime[len(prime)-i])
        else:
            i=i+1
            

print(factor(Semiprime,prime))

print(time.time() - start_time)

sys.stdout.flush()