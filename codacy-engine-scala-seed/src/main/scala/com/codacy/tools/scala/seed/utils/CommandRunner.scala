package com.codacy.tools.scala.seed.utils

import java.io._
import java.nio.charset.CodingErrorAction
import scala.collection.mutable
import scala.io.{Codec, Source}
import scala.sys.process._
import scala.util.{Failure, Success, Try}

final case class CommandResult(exitCode: Int, stdout: List[String], stderr: List[String])

object CommandRunner {

  def exec(cmd: List[String], dir: Option[File] = None): Either[Throwable, CommandResult] = {
    val stdout = mutable.Buffer[String]()
    val stderr = mutable.Buffer[String]()

    val pio = new ProcessIO(_.close(), readStream(stdout), readStream(stderr))

    def printError(): Unit = {
      System.err.println(s"Command `${cmd.mkString(" ")}` failed!")
      System.err.println("STDOUT")
      stdout.foreach(System.err.println)
      System.err.println("STDERR")
      stderr.foreach(System.err.println)
    }

    Try(Process(cmd, dir).run(pio)) match {
      case Success(process) =>
        Try(process.exitValue()) match {
          case Success(exitValue) =>
            Right(CommandResult(exitValue, stdout.toList, stderr.toList))

          case Failure(e) =>
            process.destroy()
            printError()
            Left(e)
        }

      case Failure(e) =>
        printError()
        Left(e)
    }
  }

  private def readStream(buffer: mutable.Buffer[String])(stream: InputStream): Unit = {
    implicit val codec: Codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.IGNORE)
    codec.onUnmappableCharacter(CodingErrorAction.IGNORE)

    Source
      .fromInputStream(stream)
      .getLines()
      .foreach { line =>
        buffer += line
      }

    stream.close()
  }

}
