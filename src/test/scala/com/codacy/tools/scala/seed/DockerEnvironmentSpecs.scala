package com.codacy.tools.scala.seed

import java.io.File
import java.nio.file.{Files, Paths}

import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results.Tool
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class DockerEnvironmentSpecs extends Specification {

  "DockerEnvironment" >> {

    val dockerEnvironment = new DockerEnvironment(Map.empty)

    "get the tool configuration, given a valid json file" >> {
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
        // scalafix:off NoInfer.any
        configurations must beSuccessfulTry[Option[Tool.CodacyConfiguration]](Option(expectedConfiguration))
        // scalafix:on NoInfer.any
      } finally {
        Files.delete(tempFile)
      }
    }

    "fail getting the configuration, if the json is not valid" >> {
      //given
      val tempFile = Files.createTempFile("", "")
      try {
        Files.write(tempFile, "{{invalid json}".getBytes("UTF-8"))

        //when
        val metricsConfig =
          dockerEnvironment.configurations(tempFile.toFile)

        //then
        metricsConfig must beFailedTry
      } finally {
        Files.delete(tempFile)
      }
    }

    "not return any configuration if the configuration file doesn't exist" >> {
      //given
      val nonExistentFile = Paths.get("notExistentFile.xpto").toFile

      //when
      val metricsConfig =
        dockerEnvironment.configurations(nonExistentFile)

      //then
      // scalafix:off NoInfer.any
      metricsConfig must beSuccessfulTry[Option[Tool.CodacyConfiguration]](Option.empty[Tool.CodacyConfiguration])
      // scalafix:on NoInfer.any
    }
  }
}
