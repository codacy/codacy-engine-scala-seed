package com.codacy.tools.scala.seed

import java.nio.file.{Path, Paths}
import scala.concurrent.duration._
import scala.util.{Success, Try}
import better.files._
import play.api.libs.json.Json
import com.codacy.plugins.api._
import com.codacy.plugins.api.results.Tool

import com.codacy.tools.scala.seed.traits.JsResultOps._
import com.codacy.tools.scala.seed.utils.TimeoutHelper

class DockerEnvironment(variables: Map[String, String] = sys.env) {

  val rootFile: Path = Paths.get("/src")
  val configFile: File = Paths.get("/.codacyrc")
  val specificationFile: File = Paths.get("/docs/patterns.json")

  val timeout: FiniteDuration =
    variables
      .get("TIMEOUT_SECONDS")
      .flatMap(TimeoutHelper.parseTimeout)
      .getOrElse(15.minutes)

  val debug: Boolean =
    variables.get("DEBUG").flatMap(debugStrValue => Try(debugStrValue.toBoolean).toOption).getOrElse(false)

  def configurations: Try[Option[Tool.CodacyConfiguration]] = {
    if (configFile.exists) {
      for {
        content <- Try(configFile.byteArray)
        json <- Try(Json.parse(content))
        cfg <- json.validate[Tool.CodacyConfiguration].asTry
      } yield Some(cfg)
    } else {
      Success(Option.empty[Tool.CodacyConfiguration])
    }
  }

  def specification: Try[Tool.Specification] = {
    for {
      content <- Try(specificationFile.byteArray)
      json <- Try(Json.parse(content))
      spec <- json.validate[Tool.Specification].asTry
    } yield spec
  }

}
