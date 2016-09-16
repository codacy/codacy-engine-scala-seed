package codacy.dockerApi.utils

import java.util.{Timer, TimerTask}

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.Duration
import scala.util.Try

trait Delayed {

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
