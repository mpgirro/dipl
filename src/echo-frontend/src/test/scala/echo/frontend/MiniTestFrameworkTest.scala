package echo.frontend

import minitest._

object MiniTestFrameworkTest extends SimpleTestSuite {
  test("should be") {
    assertEquals(2, 1 + 1)
  }

  test("should not be") {
    assert(1 + 1 != 3)
  }

  test("fail") {
    assert(false)
  }

  test("should throw") {
    class DummyException extends RuntimeException("DUMMY")
    def test(): String = throw new DummyException

    intercept[DummyException] {
      test()
    }
  }

  test("test result of") {
    assertResult("hello world") {
      "hello" + " " + "world"
    }
  }
}