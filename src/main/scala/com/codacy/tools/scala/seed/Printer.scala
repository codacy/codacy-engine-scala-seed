package com.codacy.tools.scala.seed

import java.io.PrintStream
import java.nio.file.Path

import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results.Result
import com.codacy.tools.scala.seed.utils.FileHelper
import play.api.libs.json.{Json, Writes}

class Printer(infoStream: PrintStream = Console.out,
              errStream: PrintStream = Console.err,
              dockerEnvironment: DockerEnvironment = new DockerEnvironment()) {

  def debug(message: String, error: Option[Throwable] = Option.empty[Throwable]): Unit = {
    if (dockerEnvironment.debug) {
      infoStream.println(message)
      error.foreach(_.printStackTrace(infoStream))
    }
  }

  def error(message: String, error: Option[Throwable] = Option.empty[Throwable]): Unit = {
    errStream.println(message)
    error.foreach(_.printStackTrace(errStream))
  }

  def results(rootFile: Path, results: List[Result]): Unit = {
    results.foreach {
      case issue: Result.Issue =>
        val relativeIssue = issue.copy(file = Source.File(relativize(rootFile, issue.file.path)))
        logResult(relativeIssue)

      case extendedIssue: Result.ExtendedIssue =>
        val relativeIssue = extendedIssue.copy(
          location =
            extendedIssue.location.copy(path = Source.File(relativize(rootFile, extendedIssue.location.path.path)))
        )
        logResult(relativeIssue)

      case error: Result.FileError =>
        val relativeIssue = error.copy(file = Source.File(relativize(rootFile, error.file.path)))
        logResult(relativeIssue)
    }
  }

  private def relativize(rootFile: Path, path: String): String =
    FileHelper.stripAbsolutePrefix(path, rootFile.toString)

  private def logResult[T](result: T)(implicit fmt: Writes[T]): Unit = {
    infoStream.println(Json.stringify(Json.toJson(result)))
  }

}
