package com.codacy.tools.scala.seed.utils

import scala.util.Try
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

object TimeoutHelper {

  def parseTimeout(timeoutSeconds: String): Option[FiniteDuration] =
    Try(FiniteDuration(timeoutSeconds.toLong, TimeUnit.SECONDS)).toOption
}
