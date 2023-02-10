import random
import time
import sys
import math
import gmpy2

Semiprime = int(sys.argv[1])


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
            if Py == 0: 
                raise ValueError("Py cannot be zero")
            s = ((3*Px*Px + A) * gmpy2.invert(2*Py, N)) %N
        else:
            if (Px-Qx) == 0: 
                raise ValueError("(Px-Qx) cannot be zero")
            s = ((Py-Qy) * gmpy2.invert((Px-Qx), N)) %N
        x = ((s*s - Px - Qx) %N)
        y = ((s*(Px - x) - Py) %N)
        return ECpoint(A,B,N, x,y)

    def __mul__(self, other):
        result = self
        for _ in range(other-1):
            result += self
        return result

    def __rmul__(self, other):
        return self * other

    def ECM(n):
        x0 = gmpy2.mpz(2)
        y0 = gmpy2.mpz(3)
        bound = max(int(gmpy2.exp(1/2*gmpy2.sqrt(gmpy2.log(n)*gmpy2.log(gmpy2.log(n))))),100)
        while True:
            a = random.randint(1,n-1)
            b = (y0*y0 - x0*x0*x0 - a*x0) %n
            if gmpy2.gcd(4*a*a*a + 27*b*b, n) != 1:
                d = gmpy2.gcd(4*a*a*a + 27*b*b, n)
                if d==n: continue
                else: return d
            P = ECpoint(a,b,n, x0,y0)
            k = 2
            while k < bound:
                P = k*P
                k *= 2
                if P.N != n:
                    return P.N


start_time = time.time()

factors=ECpoint.ECM(Semiprime)

print(factors)

print((time.time() - start_time))

sys.stdout.flush()
