//> using:
//>   lib "com.lihaoyi::utest::0.7.10"

import utest._

object HelloWorldTest extends TestSuite {
  override val tests = Tests(
    test("tests do work") {
      assert(2 * 2 == 2 + 2)
    }
  )
}
