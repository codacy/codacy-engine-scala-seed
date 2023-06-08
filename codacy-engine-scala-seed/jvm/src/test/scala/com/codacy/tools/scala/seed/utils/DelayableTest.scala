package com.codacy.tools.scala.seed.utils

import com.codacy.tools.scala.seed.traits.Delayable

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}

class DelayableTest extends munit.FunSuite with Delayable {

  test("must throw exception") {
    val f = delay(100.seconds) {
      fail("It must fail because a timeoutException wasn't thrown.")
    }

    intercept[TimeoutException] { Await.result(f, 1.second) }
  }

  test("mustn't throw exception") {
    val f = delay(1.seconds) {}

    Await.result(f, Duration.Inf)
  }
}
