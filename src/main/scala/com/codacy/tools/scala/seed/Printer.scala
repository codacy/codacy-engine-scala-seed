package com.codacy.tools.scala.seed

import java.io.PrintStream
import java.nio.file.Path

import com.codacy.plugins.api.Implicits._
import com.codacy.plugins.api.Source
import com.codacy.plugins.api.docker.v2.IssueResult
import com.codacy.tools.scala.seed.utils.FileHelper
import play.api.libs.json.Json

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

  def results(rootFile: Path, results: List[IssueResult]): Unit = {
    val relativizedResults: List[IssueResult] = results
      .map {
        case issue: IssueResult.Issue =>
          issue.copy(file = Source.File(relativize(rootFile, issue.file.path)))

        case problem: IssueResult.Problem =>
          problem.copy(file = problem.file.map(f => Source.File(relativize(rootFile, f.path))))
      }

    relativizedResults.foreach(logResult)
  }

  private def relativize(rootFile: Path, path: String): String =
    FileHelper.stripAbsolutePrefix(path, rootFile.toString)

  private def logResult(result: IssueResult): Unit = {
    infoStream.println(Json.stringify(Json.toJson(result)))
  }

}
