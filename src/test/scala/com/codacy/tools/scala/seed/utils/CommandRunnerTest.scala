package com.codacy.tools.scala.seed.utils

import scala.collection.mutable.ArrayBuffer

class CommandRunnerTest extends munit.FunSuite {

  val genericCMD = List("echo", "foo")
  val invalidCMD = List("rm", "nofile.ext")
  val errorCMD = List("rmzzz", "nofile.ext")

  test("simpleEchoExec") {
    val result: Either[Throwable, CommandResult] = CommandRunner.exec(genericCMD)

    result match {
      case Right(value) =>
        assertEquals(value.stdout, List("foo"))
        assertEquals(value.exitCode, 0)
      case Left(value) => fail(value.toString())
    }
  }

  test("handleInvalidExec") {
    val result: Either[Throwable, CommandResult] = CommandRunner.exec(invalidCMD)

    result match {
      case Right(value) =>
        assertEquals(value.stdout, List())
        assertEquals(value.exitCode, 1)
      case Left(value) => fail(value.toString())
    }
  }

  test("handleErrorExec") {
    val result: Either[Throwable, CommandResult] = CommandRunner.exec(errorCMD)

    assert(result.isLeft)
  }
}
