package com.codacy.tools.scala.seed

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future

object PlatformSpecific {

  trait DockerEngine { self: traits.Delayable with traits.Haltable =>

    def initTimeout(duration: FiniteDuration, printer: Printer): Future[Unit] = {
      printer.info("Starting timeout")
      delay(duration)(halt(2))
    }
  }
}
