package codacy.dockerApi

import play.api.libs.json.Json

import scala.util.{Failure, Success}
import DockerEnvironment._

abstract class DockerEngine(Tool: Tool) {

  def main(args: Array[String]): Unit = {
    spec.flatMap { implicit spec =>
      config.flatMap { case maybeConfig =>
        //search for our config
        val maybePatterns = maybeConfig.flatMap(_.tools.collectFirst { case config if config.name == spec.name => config.patterns })
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
        results.map{ case result =>
          println(Json.stringify(Json.toJson(result)))
        }.toList

      case Failure(error) =>
        error.printStackTrace(Console.err)
        System.exit(1)
    }
  }
}
