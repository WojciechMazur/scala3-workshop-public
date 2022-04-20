//> using:
//>   lib "com.lihaoyi::utest::0.7.10"
//>   target { scope "test" }
package ex2
import utest._

// Convert functions to use tail recursion
def factorial(n: Int): BigInt =
  if n <= 1 then 1
  else n * factorial(n - 1)

def fibonaci(n: Int): BigInt =
  if n <= 1 then 1
  else fibonaci(n - 1) + fibonaci(n - 2)

object TailRecRefactorSuite extends TestSuite {
  override val tests = Tests {
    test("Factorial") {
      test("Gives correct answers for small integers") {
        assert(factorial(0) == 1)
        assert(factorial(1) == 1)
        assert(factorial(2) == 2)
        assert(factorial(3) == 6)
        assert(factorial(4) == 24)
        assert(factorial(5) == 120)
      }

      test("Handles large integers without StackOverflow") {
        try factorial(10 * 1000)
        catch {
          case e: StackOverflowError => assert(false)
        }
        ()
      }
    }

    // Fibonacii
    test("Fibonaci") {
      test("Gives correct answers for small integers") {
        assert(fibonaci(0) == 1)
        assert(fibonaci(1) == 1)
        assert(fibonaci(2) == 2)
        assert(fibonaci(3) == 3)
        assert(fibonaci(4) == 5)
        assert(fibonaci(5) == 8)
      }

      test("Handles large integers without StackOverflow") {
        try fibonaci(10 * 1000)
        catch {
          case e: StackOverflowError => assert(false)
        }
        ()
      }
    }
  }
}
