package com.codacy.tools.scala.seed.utils

import scala.concurrent.duration._
import org.specs2.mutable.Specification

class TimeoutHelperTest extends Specification {

  "TimeoutHelper" >> {
    "parseTimeout" >> {
      "parse seconds integers" >> {
        val result = TimeoutHelper.parseTimeout("60")
        result must beEqualTo(Some(60.seconds))
      }
      "return None in case of strings that are not integers" >> {
        TimeoutHelper.parseTimeout("") must beEqualTo(None)

        TimeoutHelper.parseTimeout("foo") must beEqualTo(None)
      }
    }
  }
}
