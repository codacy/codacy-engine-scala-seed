package com.codacy.tools.scala.seed.traits

import java.util.{Timer, TimerTask}

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.util.Try

trait Delayable {

  def delay[T](delay: Duration)(block: => T): Future[T] = {
    val promise = Promise[T]()
    val t = new Timer()
    t.schedule(new TimerTask {
      override def run(): Unit = {
        promise.complete(Try(block))
      }
    }, delay.toMillis)
    promise.future
  }

}
