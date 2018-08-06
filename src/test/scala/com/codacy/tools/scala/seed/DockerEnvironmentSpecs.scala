package com.codacy.tools.scala.seed

import better.files.File
import com.codacy.plugins.api.results.IssuesTool
import org.specs2.mutable.Specification

class DockerEnvironmentSpecs extends Specification {

  "DockerEnvironment" >> {

    val dockerEnvironment = new DockerEnvironment(Map.empty)

    //TODO: This test should use a pre-made json and not read what it writes. This is not testing anything except play json.

//    "get the tool configuration, given a valid json file" >> {
//      //given
//      (for {
//        tempFile <- File.temporaryFile()
//      } yield {
//        val expectedConfiguration =
//          IssuesTool.CodacyConfiguration(Set.empty[IssuesTool.Configuration],
//                                         Some(Set(Source.File(s"${tempFile.parent.pathAsString}/a.scala"))),
//                                         Some(Map.empty))
//        tempFile.write(Json.stringify(Json.toJson(expectedConfiguration)))
//
//        //when
//        val configurations = dockerEnvironment.configurations(tempFile)
//
//        //then
//        // scalafix:off NoInfer.any
//        configurations must beSuccessfulTry[Option[IssuesTool.CodacyConfiguration]](Option(expectedConfiguration))
//        // scalafix:on NoInfer.any
//      }).get()
//    }

    "fail getting the configuration, if the json is not valid" >> {
      //given
      (for {
        tempFile <- File.temporaryFile()
      } yield {
        tempFile.write("{{invalid json}")

        //when
        val metricsConfig =
          dockerEnvironment.configurations(tempFile)

        //then
        metricsConfig must beFailedTry
      }).get()
    }

    "not return any configuration if the configuration file doesn't exist" >> {
      //given
      val nonExistentFile = File("notExistentFile.xpto")

      //when
      val metricsConfig =
        dockerEnvironment.configurations(nonExistentFile)

      //then
      // scalafix:off NoInfer.any
      metricsConfig must beSuccessfulTry[Option[IssuesTool.CodacyConfiguration]](
        Option.empty[IssuesTool.CodacyConfiguration]
      )
      // scalafix:on NoInfer.any
    }
  }
}
