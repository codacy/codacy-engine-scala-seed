package com.codacy.tools.scala.seed

import better.files.File
import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results.Tool
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class DockerEnvironmentSpecs extends Specification {

  "DockerEnvironment" >> {

    "get the tool configuration, given a valid json file" >> {
      //given
      (for {
        tempFile <- File.temporaryFile()
      } yield {
        val dockerEnvironment = new DockerEnvironment(Map.empty) {
          override val configFile: File = tempFile
        }

        val expectedConfiguration =
          Tool.CodacyConfiguration(Set.empty[Tool.Configuration],
                                   Some(Set(Source.File(s"${tempFile.parent.pathAsString}/a.scala"))),
                                   Some(Map.empty))
        tempFile.write(Json.stringify(Json.toJson(expectedConfiguration)))

        //when
        val configurations = dockerEnvironment.configurations

        //then
        // scalafix:off NoInfer.any
        configurations must beSuccessfulTry[Option[Tool.CodacyConfiguration]](Option(expectedConfiguration))
        // scalafix:on NoInfer.any
      }).get()
    }

    "fail getting the configuration, if the json is not valid" >> {
      //given
      (for {
        tempFile <- File.temporaryFile()
      } yield {
        val dockerEnvironment = new DockerEnvironment(Map.empty) {
          override val configFile: File = tempFile
        }

        tempFile.write("{{invalid json}")

        //when
        val metricsConfig =
          dockerEnvironment.configurations

        //then
        metricsConfig must beFailedTry
      }).get()
    }

    "not return any configuration if the configuration file doesn't exist" >> {
      //given
      val dockerEnvironment = new DockerEnvironment(Map.empty) {
        override val configFile: File = File("notExistentFile.xpto")
      }

      //when
      val metricsConfig =
        dockerEnvironment.configurations

      //then
      // scalafix:off NoInfer.any
      metricsConfig must beSuccessfulTry[Option[Tool.CodacyConfiguration]](Option.empty[Tool.CodacyConfiguration])
      // scalafix:on NoInfer.any
    }
  }
}
