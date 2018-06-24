package com.codacy.tools.scala.seed.utils

import com.codacy.tools.scala.seed.traits.Delayable
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}

class DelayableTest extends Specification with Delayable {

  "Delayable" >> {
    "must throw exception" >> {
      val f = delay(100.seconds) {
        failure("It must fail because a timeoutException wasn't thrown.")
      }

      Await.result(f, 1.second) must throwA[TimeoutException]
    }

    "mustn't throw exception" >> {
      val f = delay(1.seconds) {
        success("The delay didn't throw a TimeoutException exception")
      }

      // scalafix:off NoInfer.any
      Await.result(f, Duration.Inf) must not(throwA[TimeoutException])
      // scalafix:on NoInfer.any
    }
  }
}
