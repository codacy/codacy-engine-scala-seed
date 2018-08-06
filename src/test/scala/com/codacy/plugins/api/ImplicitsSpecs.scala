package com.codacy.plugins.api

import com.codacy.plugins.api.Implicits._
import com.codacy.plugins.api.docker.v2.{IssueResult, Problem}
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.duration._

class ImplicitsSpecs extends Specification {

  "Implicit conversions" should {

    val missingConfigJsonString =
      """{"$type":"ToolProblem","message":"this is a message","reason":{"$type":"MissingConfiguration","supportedFilename":["batato"]}}"""
    val missingConfigToolProblem: IssueResult =
      IssueResult.Problem(ErrorMessage("this is a message"), None, Problem.Reason.MissingConfiguration(Set("batato")))

    val timeOutJsonString =
      """{"$type":"ToolProblem","message":"this is a message","reason":{"$type":"TimedOut","timeout":{"length":10,"unit":"SECONDS"}}}"""
    val timeOutToolProblem: IssueResult =
      IssueResult.Problem(ErrorMessage("this is a message"), None, Problem.Reason.TimedOut(10.seconds))

    "serialize ToolProblem with MissingConfiguration reason" in {
      Json.stringify(Json.toJson(missingConfigToolProblem)) shouldEqual missingConfigJsonString
    }

    "serialize ToolProblem with TimeOut reason" in {
      Json.stringify(Json.toJson(timeOutToolProblem)) shouldEqual timeOutJsonString
    }
  }

}
