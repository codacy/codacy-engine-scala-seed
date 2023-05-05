package com.codacy.tools.scala.seed.utils

import scala.concurrent.duration._

class TimeoutHelperTest extends munit.FunSuite {

  test("parseTimeout: parse seconds integers") {
    val result = TimeoutHelper.parseTimeout("60")
    assertEquals(result, Some(60.seconds))
  }
  test("parseTimeout: return None in case of strings that are not integers") {
    assertEquals(TimeoutHelper.parseTimeout(""), None)

    assertEquals(TimeoutHelper.parseTimeout("foo"), None)
  }
}
