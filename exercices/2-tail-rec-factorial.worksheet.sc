
// Zaimplementuj funckję obliczającą silnię wykorzystując rekursję ogonową 

def factorial(n: Int): BigInt = {
  def loop(n: Int, acc: BigInt): BigInt = n match {
    case 0 | 1 => acc
    case _ => loop(n - 1, acc * n)
  }
  loop(n, 1: BigInt)
}

assert(factorial(0) == 1)
assert(factorial(1) == 1)
assert(factorial(2) == 2)
assert(factorial(3) == 6)
assert(factorial(4) == 24)

// Make sure it's not prone for StackOverflow errors
assert(factorial(10000) > 0)