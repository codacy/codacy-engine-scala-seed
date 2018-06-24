package com.codacy.tools.scala.seed

import java.io.{ByteArrayOutputStream, PrintStream}

import better.files.File
import com.codacy.plugins.api.results.Result.FileError
import com.codacy.plugins.api.results.{Pattern, Tool}
import com.codacy.plugins.api.{ErrorMessage, Options, Source}
import com.codacy.tools.scala.seed.utils.FileHelper
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}

class DockerEngineSpecs extends Specification with Mockito {

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
    override def halt(code: Int): Unit = ()
  }

  "DockerEngine" >> {
    "print the file results to the given stream and exit with the code 0" >> {
      //given
      val dockerEnvironment = new StubDockerEnvironment

      val outContent = new ByteArrayOutputStream()
      val outStream = new PrintStream(outContent)
      val errContent = new ByteArrayOutputStream()
      val errStream = new PrintStream(errContent)
      val printer =
        new Printer(infoStream = outStream, errStream = errStream, dockerEnvironment = dockerEnvironment)

      val fileName = "a.scala"
      val result = FileError(Source.File(fileName), Option.empty[ErrorMessage])
      val tool = mock[Tool]
      when(
        tool
          .apply(Source.Directory("/src"), Option.empty, Option.empty, Map.empty)(stubSpec)
      ).thenAnswer((invocation: InvocationOnMock) => {
        Success(List(result))
      })
      val dockerEngine = spy(new StubEngine(tool, dockerEnvironment, printer, dockerEnvironment.defaultTimeout))

      //when
      dockerEngine.main(Array.empty)

      //then
      Json.parse(outContent.toString) must beEqualTo(
        Json.toJson(
          result.copy(
            file = Source.File(FileHelper.stripAbsolutePrefix(fileName, dockerEnvironment.defaultRootFile.toString))
          )
        )
      )
      there.was(one(dockerEngine).halt(0))
    }

    "fail if the apply method fails, print the stacktrace to the given stream and exit with the code 1" >> {
      //given
      val failedMsg = s"Failed: ${Random.nextInt()}"

      val dockerEnvironment = new StubDockerEnvironment

      val outContent = new ByteArrayOutputStream()
      val outStream = new PrintStream(outContent)
      val errContent = new ByteArrayOutputStream()
      val errStream = new PrintStream(errContent)
      val printer =
        new Printer(infoStream = outStream, errStream = errStream, dockerEnvironment = dockerEnvironment)

      val tool = mock[Tool]
      val dockerEngine = spy(new StubEngine(tool, dockerEnvironment, printer, dockerEnvironment.defaultTimeout))

      when(
        tool
          .apply(Source.Directory("/src"), Option.empty, Option.empty, Map.empty)(stubSpec)
      ).thenAnswer((invocation: InvocationOnMock) => {
        Failure(new Throwable(failedMsg))
      })

      //when
      dockerEngine.main(Array.empty)

      //then
      errContent.toString must contain(failedMsg)
      there.was(one(dockerEngine).halt(1))
    }

    "fail if the configured timeout on the system environment is too low" >> {
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

      val tool = mock[Tool]
      when(
        tool
          .apply(Source.Directory("/src"), Option.empty, Option.empty, Map.empty)(stubSpec)
      ).thenAnswer((invocation: InvocationOnMock) => {
        sleep
      })

      val dockerEngine = spy(new StubEngine(tool, dockerEnvironment, printer, 3.seconds))

      //when
      dockerEngine.main(Array.empty)

      //then
      there.was(one(dockerEngine).halt(2))
    }

  }

}
