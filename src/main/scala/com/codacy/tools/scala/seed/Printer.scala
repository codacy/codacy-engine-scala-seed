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

  def info(message: String, error: Option[Throwable] = Option.empty[Throwable]): Unit = {
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
        val relativeIssue = issue.copy(filename = Source.File(relativize(rootFile, issue.filename.path)))
        logResult(relativeIssue)

      case error: Result.FileError =>
        val relativeIssue = error.copy(filename = Source.File(relativize(rootFile, error.filename.path)))
        logResult(relativeIssue)
    }
  }

  private def relativize(rootFile: Path, path: String): String =
    FileHelper.stripAbsolutePrefix(path, rootFile.toString)

  private def logResult[T](result: T)(implicit fmt: Writes[T]): Unit = {
    infoStream.println(Json.stringify(Json.toJson(result)))
  }

}
