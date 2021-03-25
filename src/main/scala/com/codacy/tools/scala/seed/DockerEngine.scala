package com.codacy.tools.scala.seed

import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.plugins.api.{Options, Source}
import com.codacy.tools.scala.seed.traits.{Delayable, Haltable}

import java.nio.file.Paths
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

abstract class DockerEngine(tool: Tool, env: DockerEnvironment = new DockerEnvironment())(
  printer: Printer = new Printer(dockerEnvironment = env)
) extends Delayable
    with Haltable {

  def main(args: Array[String]): Unit = {
    env.timeout match {
      case Some(timeout) =>
        initTimeout(timeout)
      case _ =>
        printer.debug("There is no internal timeout set")
    }

    val result = (for {
      specification <- env.specification
      configurations <- env.configurations
    } yield {
      val toolConfiguration = getToolConfiguration(specification, configurations)
      val files = getFiles(configurations)
      val toolOptions = getToolOptions(configurations)

      printer.debug("Going to run tool")

      executeTool(specification, toolConfiguration, files, toolOptions)
    }).flatten

    printResults(result)
    ()
  }

  @SuppressWarnings(Array("CatchThrowable"))
  private def executeTool(specification: Tool.Specification,
                          toolConfiguration: Option[List[Pattern.Definition]],
                          files: Option[Set[Source.File]],
                          toolOptions: Map[Options.Key, Options.Value]) = {
    // We need to catch Throwable here to avoid JVM crashes
    // Crashes can lead to docker not exiting properly
    val result = try {
      tool.apply(source = Source.Directory(env.rootFile.toString),
                 configuration = toolConfiguration,
                 files = files,
                 options = toolOptions)(specification)
    } catch {
      case t: Throwable =>
        Failure(t)
    }
    result
  }

  private def initTimeout(duration: FiniteDuration): Future[Unit] = {
    printer.debug("Starting timeout")
    delay(duration)(halt(2))
  }

  private def getToolConfiguration(
    specification: Tool.Specification,
    configurations: Option[Tool.CodacyConfiguration]
  ): Option[List[Pattern.Definition]] = {
    for {
      configs <- configurations
      config <- configs.tools.find(_.name == specification.name)
      patterns <- config.patterns
    } yield patterns
  }

  private def getFiles(configurations: Option[Tool.CodacyConfiguration]): Option[Set[Source.File]] = {
    for {
      configs <- configurations
      files <- configs.files
    } yield
      files.map { file =>
        file.copy(path = env.rootFile.resolve(Paths.get(file.path)).toString)
      }
  }

  private def getToolOptions(configurations: Option[Tool.CodacyConfiguration]): Map[Options.Key, Options.Value] = {
    (for {
      configs <- configurations
      options <- configs.options
    } yield options).getOrElse(Map.empty[Options.Key, Options.Value])
  }

  private def printResults(toolResult: Try[List[Result]]): Unit = {
    toolResult match {
      case Success(results) =>
        printer.debug(s"Got ${results.length} results")

        printer.results(env.rootFile, results)

        printer.debug("Finished executing tool")
        halt(0)

      case Failure(error) =>
        printer.error("Error executing the tool", Option(error))
        halt(1)
    }
  }

}
