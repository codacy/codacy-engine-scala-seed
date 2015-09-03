package codacy.dockerApi

import codacy.dockerApi.DockerEnvironment._
import play.api.libs.json.{Json, Writes}

import scala.util.{Failure, Success}

abstract class DockerEngine(Tool: Tool) {

  def main(args: Array[String]): Unit = {
    spec.flatMap { implicit spec =>
      config.flatMap { case maybeConfig =>
        //search for our config
        val maybePatterns = maybeConfig.flatMap(_.tools.collectFirst { case config if config.name == spec.name =>
          val allPatternIds = spec.patterns.map(_.patternId)
          config.patterns.filter { case pattern => allPatternIds.contains(pattern.patternId) }
        })
        val maybeFiles = maybeConfig.flatMap(_.files.map(_.map { case path =>
          sourcePath.resolve(path.value)
        }))

        Tool(
          path = sourcePath,
          conf = maybePatterns,
          files = maybeFiles
        )
      }
    } match {
      case Success(results) =>
        results.foreach {
          case issue: Issue =>
            val relativeIssue = issue.copy(filename = SourcePath(relativize(issue.filename.value)))
            logResult(relativeIssue)
          case error: FileError =>
            val relativeIssue = error.copy(filename = SourcePath(relativize(error.filename.value)))
            logResult(relativeIssue)
        }

      case Failure(error) =>
        error.printStackTrace(Console.err)
        System.exit(1)
    }
  }

  private def relativize(path: String) = {
    path.stripPrefix(DockerEnvironment.sourcePath.toString).stripPrefix("/")
  }

  private def logResult[T](result: T)(implicit fmt: Writes[T]) = {
    println(Json.stringify(Json.toJson(result)))
  }

}
