package com.codacy.plugins.api


import com.codacy.plugins.api.docker.v2.{IssueResult, Problem}
import com.codacy.plugins.api.docker.v2.Problem.Reason._
import com.codacy.plugins.api.languages.{Language, Languages}
import com.codacy.plugins.api.results._
import play.api.libs.json.{JsResult, _}

import scala.concurrent.duration.{FiniteDuration, TimeUnit}
import scala.language.implicitConversions
import scala.util.Try

object Implicits {

  private case class ApiFinitDuration(length: Long, unit: TimeUnit)

  private def finiteDurationFrom(mine: ApiFinitDuration) = {
    new FiniteDuration(mine.length, mine.unit)
  }
  private def serializableFromFiniteDuration(finiteDuration: FiniteDuration) = {
    ApiFinitDuration(finiteDuration.length, finiteDuration.unit)
  }

  implicit def paramValueToJsValue(paramValue: Parameter.Value): JsValue = {
    paramValue match {
      case ParamValue(v) => v
      case _ => JsNull
    }
  }

  implicit def optionsValueToJsValue(configValue: Options.Value): JsValue = {
    configValue match {
      case OptionsValue(v) => v
      case _ => JsNull
    }
  }

  @SuppressWarnings(Array("UnusedMethodParameter"))
  implicit class ParameterExtensions(param: Parameter.type) {
    def Value(jsValue: JsValue): Parameter.Value = ParamValue(jsValue)

    def Value(raw: String): Parameter.Value = Value(Try(Json.parse(raw)).getOrElse(JsString(raw)))
  }

  @SuppressWarnings(Array("UnusedMethodParameter"))
  implicit class OptionsExtensions(config: Options.type) {
    def Value(jsValue: JsValue): Options.Value = OptionsValue(jsValue)

    def Value(raw: String): Options.Value = Value(Try(Json.parse(raw)).getOrElse(JsString(raw)))
  }

  def enumWrites[E <: Enumeration#Value]: Writes[E] = Writes((e: E) => Json.toJson(e.toString))

  def enumReads[E <: Enumeration](e: E): Reads[e.Value] = {
    Reads.StringReads.flatMap { value =>
      Reads(
        (_: JsValue) =>
          e.values
            .collectFirst {
              case enumValue if enumValue.toString == value =>
                JsSuccess(enumValue)
            }
            .getOrElse(JsError(s"Invalid enumeration value $value"): JsResult[e.Value])
      )
    }
  }

  implicit lazy val parameterValueFormat: Format[Parameter.Value] =
    Format(implicitly[Reads[JsValue]].map(Parameter.Value), Writes(paramValueToJsValue))

  implicit lazy val configurationValueFormat: Format[Options.Value] =
    Format(implicitly[Reads[JsValue]].map(Options.Value), Writes(optionsValueToJsValue))

  implicit lazy val resultLinesFormat: Format[IssueResult.Lines] =
    Json.format[IssueResult.Lines]
  implicit lazy val resultPositionFormat: Format[IssueResult.Position] =
    Json.format[IssueResult.Position]
  implicit lazy val resultPositionsFormat: Format[IssueResult.Positions] =
    Json.format[IssueResult.Positions]
  implicit lazy val resultLocaltionFormat: Format[IssueResult.Location] =
    Json.format[IssueResult.Location]
  implicit lazy val resultLevelFormat: Format[IssueResult.Level.Value] =
    Format(enumReads(IssueResult.Level), enumWrites[IssueResult.Level.Value])
  implicit lazy val patternCategoryFormat: Format[Pattern.Category.Value] =
    Format(enumReads(Pattern.Category), enumWrites[Pattern.Category])

  implicit lazy val patternLanguageFormat: Format[Language] =
    Format(
      {
        Reads.StringReads.reads(_).flatMap { string =>
          Languages
            .fromName(string)
            .fold[JsResult[Language]](JsError(s"Could not find language for name $string"))(JsSuccess(_))
        }
      },
      Writes((v: Language) => Json.toJson(v.name))
    )

  implicit lazy val patternIdFormat: Format[Pattern.Id] =
    Format(Reads.StringReads.map(Pattern.Id), Writes((v: Pattern.Id) => Json.toJson(v.value)))

  implicit lazy val errorMessageFormat: Format[ErrorMessage] =
    Format(Reads.StringReads.map(ErrorMessage), Writes((v: ErrorMessage) => Json.toJson(v.value)))

  implicit lazy val resultMessageFormat: Format[IssueResult.Message] =
    Format(Reads.StringReads.map(IssueResult.Message), Writes((v: IssueResult.Message) => Json.toJson(v.value)))

  implicit lazy val resultLineFormat: Format[Source.Line] =
    Format(Reads.IntReads.map(Source.Line), Writes((v: Source.Line) => Json.toJson(v.value)))

  implicit lazy val parameterNameFormat: Format[Parameter.Name] =
    Format(Reads.StringReads.map(Parameter.Name), Writes((v: Parameter.Name) => Json.toJson(v.value)))

  implicit lazy val toolNameFormat: Format[IssuesTool.Name] =
    Format(Reads.StringReads.map(IssuesTool.Name), Writes((v: IssuesTool.Name) => Json.toJson(v.value)))

  implicit lazy val toolVersionFormat: Format[IssuesTool.Version] =
    Format(Reads.StringReads.map(IssuesTool.Version), Writes((v: IssuesTool.Version) => Json.toJson(v.value)))

  implicit lazy val sourceFileFormat: Format[Source.File] =
    Format(Reads.StringReads.map(Source.File), Writes((v: Source.File) => Json.toJson(v.path)))

  implicit lazy val parameterDescriptionTextFormat: Format[Parameter.DescriptionText] = Format(
    Reads.StringReads.map(Parameter.DescriptionText),
    Writes((v: Parameter.DescriptionText) => Json.toJson(v.value))
  )

  implicit lazy val patternDescriptionTextFormat: Format[Pattern.DescriptionText] =
    Format(Reads.StringReads.map(Pattern.DescriptionText), Writes((v: Pattern.DescriptionText) => Json.toJson(v.value)))

  implicit lazy val patternTitleFormat: Format[Pattern.Title] =
    Format(Reads.StringReads.map(Pattern.Title), Writes((v: Pattern.Title) => Json.toJson(v.value)))

  implicit lazy val patternTimeToFixFormat: Format[Pattern.TimeToFix] =
    Format(Reads.IntReads.map(Pattern.TimeToFix), Writes((v: Pattern.TimeToFix) => Json.toJson(v.value)))

  implicit lazy val parameterSpecificationFormat: Format[Parameter.Specification] = Json.format[Parameter.Specification]
  implicit lazy val parameterDefinitionFormat: Format[Parameter.Definition] = Json.format[Parameter.Definition]
  implicit lazy val patternDefinitionFormat: Format[Pattern.Definition] = Json.format[Pattern.Definition]
  implicit lazy val patternSpecificationFormat: Format[Pattern.Specification] = Json.format[Pattern.Specification]
  implicit lazy val toolConfigurationFormat: Format[IssuesTool.Configuration] = Json.format[IssuesTool.Configuration]
  implicit lazy val specificationFormat: Format[IssuesTool.Specification] = Json.format[IssuesTool.Specification]
  implicit lazy val configurationOptionsKeyFormat: Format[Options.Key] = Json.format[Options.Key]
  implicit lazy val configurationOptionsFormat: Format[Map[Options.Key, Options.Value]] =
    Format[Map[Options.Key, Options.Value]](
      Reads { json: JsValue =>
        JsSuccess(json.asOpt[Map[String, JsValue]].fold(Map.empty[Options.Key, Options.Value]) {
          _.map {
            case (k, v) =>
              Options.Key(k) -> Options.Value(v)
          }
        })
      },
      Writes(m => JsObject(m.collect { case (k, v: OptionsValue) => k.value -> v.value }))
    )
  implicit lazy val parameterDescriptionFormat: Format[Parameter.Description] = Json.format[Parameter.Description]
  implicit lazy val patternDescriptionFormat: Format[Pattern.Description] = Json.format[Pattern.Description]

  implicit val timeUnit: Format[TimeUnit] = Format(Reads[TimeUnit] { stringUnit: JsValue =>
    stringUnit.validate[String].map(x => java.util.concurrent.TimeUnit.valueOf(x))
  }, Writes[TimeUnit] { timeUnit: TimeUnit =>
    JsString(timeUnit.toString)
  })

  implicit val durationFmt: Format[FiniteDuration] =
    Format(Json.reads[ApiFinitDuration].map(finiteDurationFrom), Writes[FiniteDuration] { finiteDuration =>
      Json.writes[ApiFinitDuration].writes(serializableFromFiniteDuration(finiteDuration))
    })

  implicit val parameterProblemFmt: Format[Problem.Reason.ParameterProblem] = Json.format[Problem.Reason.ParameterProblem]
  implicit val optionProblemFmt: Format[Problem.Reason.OptionProblem] = Json.format[Problem.Reason.OptionProblem]

  implicit val missingConfigurationFmt: Format[Problem.Reason.MissingConfiguration] = Json.format[Problem.Reason.MissingConfiguration]
  implicit val invalidConfigurationFmt: Format[Problem.Reason.InvalidConfiguration] = Json.format[Problem.Reason.InvalidConfiguration]
  implicit val missingOptionsFmt: Format[Problem.Reason.MissingOptions] = Json.format[Problem.Reason.MissingOptions]
  implicit val invalidOptionsFmt: Format[Problem.Reason.InvalidOptions] = Json.format[Problem.Reason.InvalidOptions]
  implicit val timeoutFmt: Format[Problem.Reason.TimedOut] = Json.format[Problem.Reason.TimedOut]
  implicit val missingArtifactsFmt: Format[Problem.Reason.MissingArtifacts] = Json.format[Problem.Reason.MissingArtifacts]
  implicit val invalidArtifactsFmt: Format[Problem.Reason.InvalidArtifacts] = Json.format[Problem.Reason.InvalidArtifacts]
  implicit val otherReasonFmt: Format[Problem.Reason.OtherReason] = Json.format[Problem.Reason.OtherReason]

  implicit val analysisProblemReason: Writes[Problem.Reason] = ???

  implicit val IssueResultFmt: Writes[IssueResult] = ???

  implicit lazy val toolCodacyConfigurationFormat: Format[IssuesTool.CodacyConfiguration] =
    Json.format[IssuesTool.CodacyConfiguration]

}
