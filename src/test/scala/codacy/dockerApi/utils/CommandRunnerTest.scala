package codacy.dockerApi.utils

import org.scalatest._
import org.scalatest.EitherValues._

import scala.collection.mutable.ArrayBuffer

class CommandRunnerTest extends FlatSpec with Matchers {

  val genericCMD = Seq("echo", "foo")
  val invalidCMD = Seq("rm", "nofile.ext")
  val errorCMD = Seq("rmzzz", "nofile.ext")

  "CommandRunner" should "simpleEchoExec" in {
    val result: Either[Throwable, CommandResult] = CommandRunner.exec(genericCMD)

    result.right.value.stdout should be(ArrayBuffer("foo"))
    result.right.value.exitCode should be(0)
  }

  "CommandRunner" should "handleInvalidExec" in {
    val result: Either[Throwable, CommandResult] = CommandRunner.exec(invalidCMD)

    result.right.value.stdout should be(ArrayBuffer())
    result.right.value.exitCode should be(1)
  }

  "CommandRunner" should "handleErrorExec" in {
    val result: Either[Throwable, CommandResult] = CommandRunner.exec(errorCMD)

    result should be('left)
  }
}