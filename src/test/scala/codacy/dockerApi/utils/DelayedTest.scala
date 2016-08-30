package codacy.dockerApi.utils

import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}

class DelayedTest extends FlatSpec with Matchers with Delayed {

  "DelayedTest" should "should throw exception" in {
    an[TimeoutException] should be thrownBy {
      val f = delay(1.seconds) {
        throw new TimeoutException("Execution timeout")
      }

      Await.result(f, Duration.Inf)
    }
  }

  it should "shouldn't throw exception" in {
    delay(2.seconds) {
      fail("That expression shouldn't have thrown a MyExceptionType exception")
    }
  }

}


