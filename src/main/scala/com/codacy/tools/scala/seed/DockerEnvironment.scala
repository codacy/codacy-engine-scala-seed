package com.codacy.tools.scala.seed

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.duration._
import scala.util.{Success, Try}
import play.api.libs.json.Json
import com.codacy.plugins.api._
import com.codacy.plugins.api.results.Tool

import com.codacy.tools.scala.seed.traits.JsResultOps._
import com.codacy.tools.scala.seed.utils.TimeoutHelper

class DockerEnvironment(variables: Map[String, String] = sys.env) {

  val defaultRootFile: Path = Paths.get("/src")
  val defaultConfigFile: Path = Paths.get("/config/codacy.json")
  val legacyConfigFile: Path = Paths.get("/.codacyrc")
  val defaultSpecificationFile: Path = Paths.get("/docs/patterns.json")

  val defaultTimeout: FiniteDuration =
    variables
      .get("TIMEOUT_SECONDS")
      .flatMap(TimeoutHelper.parseTimeout)
      .getOrElse(15.minutes)

  val debug: Boolean =
    variables.get("DEBUG").flatMap(debugStrValue => Try(debugStrValue.toBoolean).toOption).getOrElse(false)

  def configurations(configFile: File = defaultConfigFile.toFile): Try[Option[Tool.CodacyConfiguration]] = {
    if (configFile.exists) {
      for {
        content <- Try(Files.readAllBytes(configFile.toPath))
        json <- Try(Json.parse(content))
        cfg <- json.validate[Tool.CodacyConfiguration].asTry
      } yield Some(cfg)
    } else {
      Success(Option.empty[Tool.CodacyConfiguration])
    }
  }

  def specification(specificationPath: File = defaultSpecificationFile.toFile): Try[Tool.Specification] = {
    for {
      content <- Try(Files.readAllBytes(specificationPath.toPath))
      json <- Try(Json.parse(content))
      spec <- json.validate[Tool.Specification].asTry
    } yield spec
  }

}
