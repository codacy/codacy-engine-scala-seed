package com.codacy.tools.scala.seed

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

object PlatformSpecific {

  trait DockerEngine {

    def initTimeout(duration: FiniteDuration, printer: Printer): Future[Unit] = {
      // We don't don anything in Scala Native since without threads
      // we can't implement a proper sync timeout.
      // We rely on Kubernetes already killing tools even if they don't
      // exit theirselves.
      Promise[Unit]().future
    }
  }
}
