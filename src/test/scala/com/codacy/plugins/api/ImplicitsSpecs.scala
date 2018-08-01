package com.codacy.plugins.api

import com.codacy.plugins.api.AnalysisProblem.{MissingConfiguration, TimedOut}
import com.codacy.plugins.api.Implicits._
import com.codacy.plugins.api.results.ToolResult
import com.codacy.plugins.api.results.ToolResult.ToolProblem
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.duration._

class ImplicitsSpecs extends Specification {

  "Implicit conversions" should {

    val missingConfigJsonString =
      """{"$type":"ToolProblem","message":"this is a message","reason":{"$type":"MissingConfiguration","supportedFilename":["batato"]}}"""
    val missingConfigToolProblem: ToolResult =
      ToolProblem(ErrorMessage("this is a message"), None, MissingConfiguration(Set("batato")))

    val timeOutJsonString =
      """{"$type":"ToolProblem","message":"this is a message","reason":{"$type":"TimedOut","timeout":{"length":10,"unit":"SECONDS"}}}"""
    val timeOutToolProblem: ToolResult =
      ToolProblem(ErrorMessage("this is a message"), None, TimedOut(10.seconds))

    "deserialize ToolProblem with MissingConfiguration reason" in {
      Json.parse(missingConfigJsonString).asOpt[ToolResult] should beSome(missingConfigToolProblem)
    }

    "serialize ToolProblem with MissingConfiguration reason" in {
      Json.stringify(Json.toJson(missingConfigToolProblem)) shouldEqual missingConfigJsonString
    }

    "deserialize ToolProblem with TimeOut reason" in {
      Json.parse(timeOutJsonString).asOpt[ToolResult] should beSome(timeOutToolProblem)
    }

    "serialize ToolProblem with TimeOut reason" in {
      Json.stringify(Json.toJson(timeOutToolProblem)) shouldEqual timeOutJsonString
    }
  }

}
