from random import randint
from math import e as exp
from math import sqrt, log
import gmpy2
import sys
import time


def Int(x):
    return int(x) if gmpy2 is None else gmpy2.mpz(x)

class InvError(Exception):
    def __init__(self, v):
        self.value = v

Semiprime = int(sys.argv[1])

def inv(a, n):
    a %= n
    if gmpy2 is None:
        try:
            return pow(a, -1, n)
        except ValueError:
            import math
            raise InvError(gmpy2.gcdext(a, n))
    else:
        g, s, t = gmpy2.gcdext(a, n)
        if g != 1:
            raise InvError(g)
        return s % n


class ECpoint(object):
    def __init__(self, A,B,N, x,y):
        if (y*y - x*x*x - A*x - B)%N != 0: raise ValueError
        self.A, self.B = A, B
        self.N = N
        self.x, self.y = x, y

    def __add__(self, other):
        A,B,N = self.A, self.B, self.N
        Px, Py, Qx, Qy = self.x, self.y, other.x, other.y
        if Px == Qx and Py == Qy:
            s = Int((3*Px*Px + A)%N * inv((2*Py)%N, N) %N)
        else:
            s = Int((Py-Qy)%N * inv((Px-Qx)%N, N) %N)
        x = Int((s*s - Px - Qx) %N)
        y = Int((s*(Px - x) - Py) %N)
        return ECpoint(A,B,N, x,y)

    def __rmul__(self, other):
        r = self; other -= 1
        while True:
            if other & 1:
                r = r + self
                if other==1: return r
            other >>= 1
            self = self+self

# 848823571102059028711717116259
# 6567000046404686989
def ECM(n):
    x0 = Int(2)
    y0 = Int(3)
    bound =Int(max(int(exp**(1/2*sqrt(log(n)*log(log(n))))),100))
    while True:
        try:
            a = randint(1,n-1)
            inv(a,n)
            b = (y0*y0 - x0*x0*x0 - a*x0) %n
            inv(b,n)
            inv((4*a*a*a + 27*b*b)%n, n)

            P = ECpoint(a,b,n, x0,y0)
            for k in range(2, bound):
                P = k*P
                #print(k,P)

        except InvError as e:
            d = e.value
            if d==n: continue
            else: return d

start_time = time.time()

factors=ECM(Semiprime)

print(factors)

print((time.time() - start_time))

sys.stdout.flush()