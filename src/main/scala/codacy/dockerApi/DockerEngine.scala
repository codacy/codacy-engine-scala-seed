package codacy.dockerApi

import akka.actor.ActorSystem
import codacy.dockerApi.DockerEnvironment._
import play.api.libs.json.{Json, Writes}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

abstract class DockerEngine(Tool: Tool) {

  lazy val sys = ActorSystem("timeoutSystem")

  def initTimeout(duration: FiniteDuration) = {
    implicit val ct: ExecutionContext = sys.dispatcher
    sys.scheduler.scheduleOnce(duration){
      Runtime.getRuntime().halt(1)
    }
  }

  lazy val timeout = Option(System.getProperty("timeout")).flatMap{ case rawDuration =>
    Try(Duration(rawDuration)).toOption.collect{ case d:FiniteDuration => d }
  }.getOrElse(30.minutes)

  def main(args: Array[String]): Unit = {
    initTimeout(timeout)

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
