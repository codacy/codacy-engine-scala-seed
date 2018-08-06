package com.codacy.tools.scala.seed

import java.nio.file.{Path, Paths}

import better.files._
import com.codacy.plugins.api.Implicits._
import com.codacy.plugins.api.results.IssuesTool
import com.codacy.tools.scala.seed.traits.JsResultOps._
import play.api.libs.json.Json

import scala.concurrent.duration.{Duration, FiniteDuration, _}
import scala.util.{Success, Try}

class DockerEnvironment(variables: Map[String, String] = sys.env) {

  val defaultRootFile: Path = Paths.get("/src")
  val defaultConfigFile: Path = Paths.get("/.codacyrc")
  val defaultSpecificationFile: Path = Paths.get("/docs/patterns.json")

  val defaultTimeout: FiniteDuration =
    variables
      .get("TIMEOUT")
      .flatMap(
        timeoutStrValue =>
          Try(Duration(timeoutStrValue)).toOption.collect {
            case d: FiniteDuration => d
        }
      )
      .getOrElse(15.minutes)

  val debug: Boolean =
    variables.get("DEBUG").flatMap(debugStrValue => Try(debugStrValue.toBoolean).toOption).getOrElse(false)

  def configurations(configFile: File = defaultConfigFile): Try[Option[IssuesTool.CodacyConfiguration]] = {
    if (configFile.exists) {
      for {
        content <- Try(configFile.byteArray)
        json <- Try(Json.parse(content))
        cfg <- json.validate[IssuesTool.CodacyConfiguration].asTry
      } yield Some(cfg)
    } else {
      Success(Option.empty[IssuesTool.CodacyConfiguration])
    }
  }

  def specification(specificationPath: File = defaultSpecificationFile): Try[IssuesTool.Specification] = {
    for {
      content <- Try(specificationPath.byteArray)
      json <- Try(Json.parse(content))
      spec <- json.validate[IssuesTool.Specification].asTry
    } yield spec
  }

}
