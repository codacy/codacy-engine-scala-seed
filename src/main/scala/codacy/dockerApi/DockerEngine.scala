package codacy.dockerApi

import java.nio.file.Paths

import codacy.docker.api.{Configuration, Source, Result => NewResult}
import codacy.dockerApi.DockerEnvironment._
import codacy.dockerApi.utils.Delayed
import play.api.libs.json.{Json, Writes}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

abstract class DockerEngine(Tool: codacy.docker.api.Tool) extends Delayed {

  def initTimeout(duration: FiniteDuration): Future[Unit] = {
    delay(duration) {
      Runtime.getRuntime.halt(2)
    }
  }

  lazy val timeout: FiniteDuration = Option(System.getProperty("timeout")).flatMap { rawDuration =>
    Try(Duration(rawDuration)).toOption.collect { case d: FiniteDuration => d }
  }.getOrElse(10.minutes)

  lazy val isDebug: Boolean = Option(System.getProperty("debug")).flatMap { rawDebug =>
    Try(rawDebug.toBoolean).toOption
  }.getOrElse(false)

  def log(message: String): Unit = if (isDebug) {
    Console.err.println(s"[DockerEngine] $message")
  }

  def main(args: Array[String]): Unit = {
    log("starting timeout")
    initTimeout(timeout)

    for {
      spec <- specification
      configOpt <- configuration
    } yield {

      val patternsOpt = for {
        config <- configOpt
        toolCfg <- config.tools.find(_.name == spec.name)
        patterns <- toolCfg.patterns
      } yield patterns

      val filesOpt = for {
        config <- configOpt
        files <- config.files
      } yield {
        //TODO: i see a problem with the .toString here, also convert it to better-files ops please!
        files.map { file => file.copy(path = sourcePath.path.resolve(Paths.get(file.path)).toString) }
      }

      log("tool started")
      // We need to catch Throwable here to avoid JVM crashes
      // Crashes can lead to docker not exiting properly
      val res = try {
        Tool.apply(
          source = Source.Directory(sourcePath.toString()),
          configuration = patternsOpt,
          files = filesOpt,
          options = configOpt.flatMap(_.options).getOrElse(Map.empty)
        )(spec)
      } catch {
        case t: Throwable =>
          Failure(t)
      }

      res match {
        case Success(results) =>
          log("receiving results")
          results.foreach {
            case issue@NewResult.Issue(_, _, _, _) =>
              val relativeIssue = issue.copy(file = Source.File(relativize(issue.file.path)))
              logResult(relativeIssue)
            case error@NewResult.FileError(_, _) =>
              val relativeIssue = error.copy(file = Source.File(relativize(error.file.path)))
              logResult(relativeIssue)
          }
          log("tool finished")
          System.exit(0)
        case Failure(error) =>
          error.printStackTrace(Console.err)
          Runtime.getRuntime.halt(1)
      }
    }
  }

  private def relativize(path: String) = {
    path.stripPrefix(DockerEnvironment.sourcePath.toString).stripPrefix("/")
  }

  private def logResult[T](result: T)(implicit fmt: Writes[T]) = {
    println(Json.stringify(Json.toJson(result)))
  }

}
