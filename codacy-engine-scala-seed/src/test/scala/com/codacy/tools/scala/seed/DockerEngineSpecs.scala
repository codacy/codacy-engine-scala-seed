package com.codacy.tools.scala.seed

import java.io.{ByteArrayOutputStream, File, PrintStream}

import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.plugins.api._
import com.codacy.tools.scala.seed.utils.FileHelper
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.collection.mutable
import scala.util.{Failure, Random, Success, Try}

class DockerEngineSpecs extends munit.FunSuite {

  private val stubSpec =
    Tool.Specification(Tool.Name("Name"), Option(Tool.Version("1.0.0-SNAPSHOT")), Set.empty[Pattern.Specification])

  class StubDockerEnvironment extends DockerEnvironment(Map.empty) {
    override def specification(specificationPath: File): Try[Tool.Specification] =
      Success(stubSpec)
    override def configurations(configFile: File): Try[Option[Tool.CodacyConfiguration]] =
      Success(
        Option(
          Tool.CodacyConfiguration(Set.empty[Tool.Configuration],
                                   Option.empty[Set[Source.File]],
                                   Option.empty[Map[Options.Key, Options.Value]])
        )
      )
  }

  class StubEngine(tool: Tool,
                   dockerEnvironment: DockerEnvironment,
                   printer: Printer,
                   timeout: FiniteDuration = 10.minutes)
      extends DockerEngine(tool, dockerEnvironment)(printer = printer, timeout = timeout) {
    private val times = mutable.Map.empty[Int, Int]
    def haltCalls = times
    override def halt(code: Int): Unit = times.get(code) match {
      case Some(value) => times += (code -> (value + 1))
      case None => times += code -> 1
    }
  }

  test("print the file results to the given stream and exit with the code 0") {
    //given
    val dockerEnvironment = new StubDockerEnvironment

    val outContent = new ByteArrayOutputStream()
    val outStream = new PrintStream(outContent)
    val errContent = new ByteArrayOutputStream()
    val errStream = new PrintStream(errContent)
    val printer =
      new Printer(infoStream = outStream, errStream = errStream, dockerEnvironment = dockerEnvironment)

    val fileName = "a.scala"
    val result = Result.FileError(Source.File(fileName), Option.empty[ErrorMessage])
    val tool = new Tool {
      def apply(
        source: Source.Directory,
        configuration: Option[List[Pattern.Definition]],
        files: Option[Set[Source.File]],
        options: Map[Options.Key, Options.Value]
      )(implicit specification: Tool.Specification): Try[List[Result]] = Success(List(result))
    }
    val dockerEngine = new StubEngine(tool, dockerEnvironment, printer, dockerEnvironment.defaultTimeout)

    //when
    dockerEngine.main(Array.empty)

    //then
    val outContentParsed = Json.parse(outContent.toString)
    val expected = Json.toJson(
      result
        .copy(
          filename = Source.File(FileHelper.stripAbsolutePrefix(fileName, dockerEnvironment.defaultRootFile.toString))
        )
    )
    assertEquals(outContentParsed, expected)
    assertEquals(dockerEngine.haltCalls(0), 1)
  }

  test("fail if the apply method fails, print the stacktrace to the given stream and exit with the code 1") {
    //given
    val failedMsg = s"Failed: ${Random.nextInt()}"

    val dockerEnvironment = new StubDockerEnvironment

    val outContent = new ByteArrayOutputStream()
    val outStream = new PrintStream(outContent)
    val errContent = new ByteArrayOutputStream()
    val errStream = new PrintStream(errContent)
    val printer =
      new Printer(infoStream = outStream, errStream = errStream, dockerEnvironment = dockerEnvironment)

    val tool = new Tool {
      def apply(
        source: Source.Directory,
        configuration: Option[List[Pattern.Definition]],
        files: Option[Set[Source.File]],
        options: Map[Options.Key, Options.Value]
      )(implicit specification: Tool.Specification): Try[List[Result]] = Failure(new Throwable(failedMsg))
    }
    val dockerEngine = new StubEngine(tool, dockerEnvironment, printer, dockerEnvironment.defaultTimeout)

    // tool(Source.Directory("/src"), Option.empty, Option.empty, Map.empty)(stubSpec))

    //when
    dockerEngine.main(Array.empty)

    //then
    assert(errContent.toString.contains(failedMsg))
    assertEquals(dockerEngine.haltCalls(1), 1)
  }

  test("fail if the configured timeout on the system environment is too low") {
    //given
    val dockerEnvironment = new StubDockerEnvironment

    val outContent = new ByteArrayOutputStream()
    val outStream = new PrintStream(outContent)
    val errContent = new ByteArrayOutputStream()
    val errStream = new PrintStream(errContent)
    val printer =
      new Printer(infoStream = outStream, errStream = errStream, dockerEnvironment = dockerEnvironment)

    def sleep = {
      Thread.sleep(10.seconds.toMillis)
      Success(List.empty)
    }

    val tool = new Tool {
      def apply(
        source: Source.Directory,
        configuration: Option[List[Pattern.Definition]],
        files: Option[Set[Source.File]],
        options: Map[Options.Key, Options.Value]
      )(implicit specification: Tool.Specification): Try[List[Result]] = sleep
    }

    val dockerEngine = new StubEngine(tool, dockerEnvironment, printer, 3.seconds)

    //when
    dockerEngine.main(Array.empty)

    //then
    assertEquals(dockerEngine.haltCalls(2), 1)
  }
}
