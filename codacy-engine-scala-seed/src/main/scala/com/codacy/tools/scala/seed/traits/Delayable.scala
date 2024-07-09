package com.codacy.tools.scala.seed.traits

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.util.Try

trait Delayable {

  def delay[T](delay: Duration)(block: => T): Future[T] = {
    val promise = Promise[T]()
    new Thread(new Runnable {
      override def run(): Unit = {
        Thread.sleep(delay.toMillis)
        promise.complete(Try(block))
      }
    }).start()
    promise.future
  }

}
