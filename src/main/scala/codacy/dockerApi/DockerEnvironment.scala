package codacy.dockerApi

import java.nio.file.{Files, Paths}

import better.files._
import codacy.docker.api.{Configuration, Tool => NewTool}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, Json}

import scala.util.{Failure, Success, Try}

object DockerEnvironment {

  lazy val configuration: Try[Option[Configuration]] = {
    if (configFilePath.isRegularFile) {
      for {
        content <- Try(configFilePath.byteArray)
        json <- Try(Json.parse(content))
        cfg <- json.validate[Configuration].asTry
      } yield Some(cfg)
    } else {
      Success(Option.empty[Configuration])
    }
  }

  lazy val specification: Try[NewTool.Specification] = {
    for {
      content <- Try(specificationPath.byteArray)
      json <- Try(Json.parse(content))
      spec <- json.validate[NewTool.Specification].asTry
    } yield spec
  }

  //TODO: check why we returned an empty config on json parse error before
  @deprecated("use configuration instead", "2.7.0")
  def config(implicit spec: Spec): Try[Option[FullConfig]] = Try(Files.readAllBytes(configFilePath.path)).transform(
    raw => Try(Json.parse(raw)).flatMap(
      _.validate[FullConfig].fold(
        asFailure,
        conf => Success(Option(conf))
      )),
    _ => Success(Option.empty[FullConfig])
  )

  @deprecated("use specification instead", "2.7.0")
  lazy val spec: Try[Spec] = {
    Try(
      Files.readAllBytes(Paths.get("/docs/patterns.json"))
    ).flatMap { case bytes =>
      Try(Json.parse(bytes)).flatMap(_.validate[Spec].fold(
        asFailure,
        Success.apply
      ))
    }
  }

  private[this] def asFailure(error: Seq[(JsPath, Seq[ValidationError])]) =
    Failure(new Throwable(Json.stringify(JsError.toJson(error.toList))))

  private[dockerApi] lazy val configFilePath = sourcePath / ".codacy.json"
  private[dockerApi] lazy val sourcePath = File("/src")
  private[dockerApi] lazy val specificationPath = File("/docs/patterns.json")
}
