package com.codacy.plugins.api

import com.codacy.plugins.api.docker.v2.{IssueResult, Problem}
import com.codacy.plugins.api.languages.{Language, Languages}
import com.codacy.plugins.api.results._
import play.api.libs.json.{JsResult, _}

import scala.concurrent.duration.{FiniteDuration, TimeUnit}
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.Try

object Implicits {

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

  // Docker Output API

  implicit lazy val resultLinesFormat: Writes[IssueResult.Lines] = Json.writes[IssueResult.Lines]
  implicit lazy val resultPositionFormat: Writes[IssueResult.Position] = Json.writes[IssueResult.Position]
  implicit lazy val resultPositionsFormat: Writes[IssueResult.Positions] = Json.writes[IssueResult.Positions]
  implicit lazy val resultLocaltionFormat: Writes[IssueResult.Location] = Json.writes[IssueResult.Location]
  implicit lazy val resultLevelFormat: Format[Pattern.Level.Value] =
    Format(enumReads(Pattern.Level), enumWrites[Pattern.Level.Value])
  implicit lazy val patternCategoryFormat: Format[Pattern.Category.Value] =
    Format(enumReads(Pattern.Category), enumWrites[Pattern.Category.Value])
  implicit lazy val patternIdFormat: Format[Pattern.Id] = {
    Format(Reads.StringReads.map(Pattern.Id), Writes((v: Pattern.Id) => Json.toJson(v.value)))
  }

  final private case class ApiFiniteDuration(length: Long, unit: TimeUnit)

  implicit val durationFmt: Format[FiniteDuration] = {
    def finiteDurationFrom(mine: ApiFiniteDuration): FiniteDuration = {
      new FiniteDuration(mine.length, mine.unit)
    }

    def serializableFromFiniteDuration(finiteDuration: FiniteDuration): ApiFiniteDuration = {
      ApiFiniteDuration(finiteDuration.length, finiteDuration.unit)
    }

    implicit val timeUnit: Format[TimeUnit] = Format(Reads[TimeUnit] { stringUnit: JsValue =>
      stringUnit.validate[String].map(x => java.util.concurrent.TimeUnit.valueOf(x))
    }, Writes[TimeUnit] { timeUnit: TimeUnit => JsString(timeUnit.toString)
    })

    Format[FiniteDuration](Json.reads[ApiFiniteDuration].map(finiteDurationFrom),
                           Writes[FiniteDuration](
                             fd => Json.writes[ApiFiniteDuration].writes(serializableFromFiniteDuration(fd))
                           ))
  }

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
  implicit lazy val errorMessageFormat: Writes[ErrorMessage] = Writes((v: ErrorMessage) => Json.toJson(v.value))
  implicit lazy val resultLineFormat: Writes[Source.Line] = Writes((v: Source.Line) => Json.toJson(v.value))
  implicit lazy val sourceFileFormat: Format[Source.File] =
    Format(Reads.StringReads.map(Source.File), Writes((v: Source.File) => Json.toJson(v.path)))

  implicit val analysisProblemReason: OWrites[Problem.Reason] = {
    implicit val parameterProblemFmt: OWrites[Problem.Reason.ParameterProblem] =
      Json.writes[Problem.Reason.ParameterProblem]
    implicit val optionProblemFmt: OWrites[Problem.Reason.OptionProblem] = Json.writes[Problem.Reason.OptionProblem]
    val missingConfigurationFmt: OWrites[Problem.Reason.MissingConfiguration] =
      Json.writes[Problem.Reason.MissingConfiguration]
    val invalidConfigurationFmt: OWrites[Problem.Reason.InvalidConfiguration] =
      Json.writes[Problem.Reason.InvalidConfiguration]
    val missingOptionsFmt: OWrites[Problem.Reason.MissingOptions] = Json.writes[Problem.Reason.MissingOptions]
    val invalidOptionsFmt: OWrites[Problem.Reason.InvalidOptions] = Json.writes[Problem.Reason.InvalidOptions]
    val timeoutFmt: OWrites[Problem.Reason.TimedOut] = Json.writes[Problem.Reason.TimedOut]
    val missingArtifactsFmt: OWrites[Problem.Reason.MissingArtifacts] =
      Json.writes[Problem.Reason.MissingArtifacts]
    val invalidArtifactsFmt: OWrites[Problem.Reason.InvalidArtifacts] =
      Json.writes[Problem.Reason.InvalidArtifacts]
    val otherReasonFmt: OWrites[Problem.Reason.OtherReason] = Json.writes[Problem.Reason.OtherReason]

    OWrites {
      case v: Problem.Reason.MissingConfiguration =>
        addType[Problem.Reason.MissingConfiguration](missingConfigurationFmt.writes(v))
      case v: Problem.Reason.InvalidConfiguration =>
        addType[Problem.Reason.InvalidConfiguration](invalidConfigurationFmt.writes(v))
      case v: Problem.Reason.MissingOptions => addType[Problem.Reason.MissingOptions](missingOptionsFmt.writes(v))
      case v: Problem.Reason.InvalidOptions => addType[Problem.Reason.InvalidOptions](invalidOptionsFmt.writes(v))
      case v: Problem.Reason.TimedOut => addType[Problem.Reason.TimedOut](timeoutFmt.writes(v))
      case v: Problem.Reason.MissingArtifacts => addType[Problem.Reason.MissingArtifacts](missingArtifactsFmt.writes(v))
      case v: Problem.Reason.InvalidArtifacts => addType[Problem.Reason.InvalidArtifacts](invalidArtifactsFmt.writes(v))
      case v: Problem.Reason.OtherReason => addType[Problem.Reason.OtherReason](otherReasonFmt.writes(v))
    }
  }

  implicit val IssueResultFmt: OWrites[IssueResult] = {
    val issueResultIssueWrites: OWrites[IssueResult.Issue] = Json.writes[IssueResult.Issue]
    val issueResultProblemWrites: OWrites[IssueResult.Problem] = Json.writes[IssueResult.Problem]

    OWrites[IssueResult] {
      case v: IssueResult.Issue => addType[IssueResult.Issue](issueResultIssueWrites.writes(v))
      case v: IssueResult.Problem => addType[IssueResult.Problem](issueResultProblemWrites.writes(v))
    }
  }

  implicit lazy val parameterValueFormat: Format[Parameter.Value] =
    Format(implicitly[Reads[JsValue]].map(Parameter.Value), Writes(paramValueToJsValue))

  implicit lazy val configurationValueFormat: Format[Options.Value] =
    Format(implicitly[Reads[JsValue]].map(Options.Value), Writes(optionsValueToJsValue))

  implicit lazy val resultMessageFormat: Format[IssueResult.Message] =
    Format(Reads.StringReads.map(IssueResult.Message), Writes((v: IssueResult.Message) => Json.toJson(v.value)))

  implicit lazy val parameterNameFormat: Format[Parameter.Name] =
    Format(Reads.StringReads.map(Parameter.Name), Writes((v: Parameter.Name) => Json.toJson(v.value)))

  implicit lazy val toolNameFormat: Format[IssuesTool.Name] =
    Format(Reads.StringReads.map(IssuesTool.Name), Writes((v: IssuesTool.Name) => Json.toJson(v.value)))

  implicit lazy val toolVersionFormat: Format[IssuesTool.Version] =
    Format(Reads.StringReads.map(IssuesTool.Version), Writes((v: IssuesTool.Version) => Json.toJson(v.value)))

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

  implicit lazy val issuesToolCodacyConfigurationFormat: Reads[IssuesTool.CodacyConfiguration] =
    Json.reads[IssuesTool.CodacyConfiguration]

  private def addType[T](jso: JsObject)(implicit ev: ClassTag[T]): JsObject = {
    jso ++ JsObject(Seq(("$type", JsString(ev.runtimeClass.getName))))
  }

}
