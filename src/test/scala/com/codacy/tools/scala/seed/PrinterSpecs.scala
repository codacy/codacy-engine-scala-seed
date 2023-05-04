package com.codacy.tools.scala.seed

import java.io.{ByteArrayOutputStream, PrintStream}

import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results.Result.Issue
import com.codacy.plugins.api.results.{Pattern, Result}
import com.codacy.tools.scala.seed.utils.FileHelper
import play.api.libs.json.Json

class PrinterSpecs extends munit.FunSuite {
  test("print the file results converted to json to the given print stream") {
    //given
    val outContent = new ByteArrayOutputStream()
    val printStream = new PrintStream(outContent)
    val printer = new Printer(printStream)
    val dockerMetricsEnvironment = new DockerEnvironment(Map.empty)
    val fileName = "a.scala"
    val sourcePath = dockerMetricsEnvironment.defaultRootFile
    val result =
      Issue(Source.File("a.scala"), Result.Message("Found issue"), Pattern.Id("pattern-id"), Source.Line(1))

    //when
    printer.results(sourcePath, List(result))

    //then
    assertEquals(
      Json.parse(outContent.toString),
      Json.toJson(result.copy(file = Source.File(FileHelper.stripAbsolutePrefix(fileName, sourcePath.toString))))
    )
  }
}
