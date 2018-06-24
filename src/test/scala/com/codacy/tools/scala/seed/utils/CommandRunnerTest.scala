package com.codacy.tools.scala.seed.utils

import org.specs2.mutable.Specification

import scala.collection.mutable.ArrayBuffer

class CommandRunnerTest extends Specification {

  val genericCMD = List("echo", "foo")
  val invalidCMD = List("rm", "nofile.ext")
  val errorCMD = List("rmzzz", "nofile.ext")

  "CommandRunner" >> {
    "simpleEchoExec" in {
      val result: Either[Throwable, CommandResult] = CommandRunner.exec(genericCMD)

      result must beLike {
        case Right(value) =>
          value.stdout must beEqualTo(ArrayBuffer("foo"))
          value.exitCode must beEqualTo(0)
      }
    }

    "handleInvalidExec" >> {
      val result: Either[Throwable, CommandResult] = CommandRunner.exec(invalidCMD)

      result must beLike {
        case Right(value) =>
          value.stdout must beEqualTo(ArrayBuffer())
          value.exitCode must beEqualTo(1)
      }
    }

    "handleErrorExec" >> {
      val result: Either[Throwable, CommandResult] = CommandRunner.exec(errorCMD)

      result must beLeft
    }
  }
}
