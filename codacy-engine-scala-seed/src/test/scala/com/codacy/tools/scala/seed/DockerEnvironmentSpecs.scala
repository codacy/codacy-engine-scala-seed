package com.codacy.tools.scala.seed

import java.io.File
import java.nio.file.{Files, Paths}

import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results.Tool
import play.api.libs.json.Json
import scala.util.Success

class DockerEnvironmentSpecs extends munit.FunSuite {

  val dockerEnvironment = new DockerEnvironment(Map.empty)

  test("get the tool configuration, given a valid json file") {
    //given
    val tempFile = Files.createTempFile("", "")
    try {
      val expectedConfiguration =
        Tool.CodacyConfiguration(Set.empty[Tool.Configuration],
                                 Some(Set(Source.File(s"${tempFile.getParent.toString}/a.scala"))),
                                 Some(Map.empty))
      Files.write(tempFile, Json.stringify(Json.toJson(expectedConfiguration)).getBytes("UTF-8"))

      //when
      val configurations = dockerEnvironment.configurations(tempFile.toFile)

      //then
      assertEquals(configurations, Success(Some(expectedConfiguration)))
    } finally {
      Files.delete(tempFile)
    }
  }

  test("fail getting the configuration, if the json is not valid") {
    //given
    val tempFile = Files.createTempFile("", "")
    try {
      Files.write(tempFile, "{{invalid json}".getBytes("UTF-8"))

      //when
      val metricsConfig =
        dockerEnvironment.configurations(tempFile.toFile)

      //then
      assert(metricsConfig.isFailure)
    } finally {
      Files.delete(tempFile)
    }
  }

  test("not return any configuration if the configuration file doesn't exist") {
    //given
    val nonExistentFile = Paths.get("notExistentFile.xpto").toFile

    //when
    val metricsConfig =
      dockerEnvironment.configurations(nonExistentFile)

    //then
    // scalafix:off NoInfer.any
    assertEquals(metricsConfig, Success(None))
    // scalafix:on NoInfer.any
  }
}
