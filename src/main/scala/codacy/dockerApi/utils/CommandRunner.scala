package codacy.dockerApi.utils

import java.io._
import java.nio.charset.CodingErrorAction

import scala.collection.mutable
import scala.io.{Codec, Source}
import scala.language.postfixOps
import scala.sys.process._
import scala.util.{Failure, Success, Try}

case class CommandResult(exitCode: Int, stdout: Seq[String], stderr: Seq[String])

object CommandRunner {

  def exec(cmd: Seq[String], dir: Option[File] = None): Either[Throwable, CommandResult] = {
    val stdout = mutable.Buffer[String]()
    val stderr = mutable.Buffer[String]()

    val pio = new ProcessIO(_.close(), readStream(stdout), readStream(stderr))

    val process = Process(cmd, dir).run(pio)
    val result = Try(process.exitValue())

    result match {
      case Success(exitValue) =>
        Right(CommandResult(exitValue, stdout, stderr))

      case Failure(e) =>
        process.destroy()
        Left(e)
    }
  }

  private def readStream(buffer: mutable.Buffer[String])(stream: InputStream) = {
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.IGNORE)
    codec.onUnmappableCharacter(CodingErrorAction.IGNORE)

    Source.fromInputStream(stream)
      .getLines()
      .foreach { line => buffer += line }
    stream.close()
  }

}
