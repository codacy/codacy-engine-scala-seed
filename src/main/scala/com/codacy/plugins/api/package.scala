package com.codacy.plugins

import scala.language.implicitConversions
import scala.util.Try
import play.api.libs.json.{JsResult, _}
import com.codacy.plugins.api.languages.{Language, Languages}
import com.codacy.plugins.api.results.{Parameter, Pattern, Result, Tool}
import scala.collection.immutable.SortedSet

package object api {

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

  implicit lazy val resultLinesFormat: Format[Result.Lines] =
    Json.format[Result.Lines]
  implicit lazy val resultPositionFormat: Format[Result.Position] =
    Json.format[Result.Position]
  implicit lazy val resultPositionsFormat: Format[Result.Positions] =
    Json.format[Result.Positions]
  implicit lazy val resultLocaltionFormat: Format[Result.Location] =
    Json.format[Result.Location]
  implicit lazy val resultLevelFormat: Format[Result.Level.Value] =
    Json.formatEnum(Result.Level)
  implicit lazy val patternCategoryFormat: Format[Pattern.Category] =
    Json.formatEnum(Pattern.Category)

  implicit lazy val patternLanguageFormat: Format[Language] =
    Format(
      Reads(
        Reads.StringReads
          .reads(_)
          .flatMap(
            string =>
              Languages
                .fromName(string)
                .fold[JsResult[Language]](JsError(s"Could not find language for name $string"))(JsSuccess(_))
          )
      ),
      Writes((v: Language) => Json.toJson(v.name))
    )

  implicit lazy val patternIdFormat: Format[Pattern.Id] =
    Format(Reads.StringReads.map(Pattern.Id), Writes((v: Pattern.Id) => Json.toJson(v.value)))

  implicit lazy val errorMessageFormat: Format[ErrorMessage] =
    Format(Reads.StringReads.map(ErrorMessage), Writes((v: ErrorMessage) => Json.toJson(v.value)))

  implicit lazy val resultMessageFormat: Format[Result.Message] =
    Format(Reads.StringReads.map(Result.Message), Writes((v: Result.Message) => Json.toJson(v.value)))

  implicit lazy val resultLineFormat: Format[Source.Line] =
    Format(Reads.IntReads.map(Source.Line), Writes((v: Source.Line) => Json.toJson(v.value)))

  implicit lazy val parameterNameFormat: Format[Parameter.Name] =
    Format(Reads.StringReads.map(Parameter.Name), Writes((v: Parameter.Name) => Json.toJson(v.value)))

  implicit lazy val toolNameFormat: Format[Tool.Name] =
    Format(Reads.StringReads.map(Tool.Name), Writes((v: Tool.Name) => Json.toJson(v.value)))

  implicit lazy val toolVersionFormat: Format[Tool.Version] =
    Format(Reads.StringReads.map(Tool.Version), Writes((v: Tool.Version) => Json.toJson(v.value)))

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

  implicit lazy val patternSubCategoryFormat: Format[Pattern.Subcategory] =
    Json.formatEnum(Pattern.Subcategory)

  implicit lazy val patternSpecificationFormat: Format[Pattern.Specification] =
    Json.format[Pattern.Specification]

  implicit lazy val toolConfigurationFormat: Format[Tool.Configuration] = Json.format[Tool.Configuration]

  implicit lazy val specificationFormat: Format[Tool.Specification] = {
    val reads = Json.reads[Tool.Specification]
    val writes = Json.writes[Tool.Specification].contramap { (s: Tool.Specification) =>
      val patternsOrdering = Ordering.by[Pattern.Specification, String](_.patternId.value)
      val emptySortedSet = SortedSet.empty[Pattern.Specification](patternsOrdering)
      s.copy(patterns = emptySortedSet ++ s.patterns)
    }
    Format[Tool.Specification](reads, writes)
  }

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

  implicit lazy val resultWrites: Writes[Result] = Writes[Result]((_: Result) match {
    case r: Result.Issue => Json.writes[Result.Issue].writes(r)
    case r: Result.ExtendedIssue => Json.writes[Result.ExtendedIssue].writes(r)
    case e: Result.FileError => Json.writes[Result.FileError].writes(e)
  })

  implicit lazy val toolCodacyConfigurationFormat: Format[Tool.CodacyConfiguration] =
    Json.format[Tool.CodacyConfiguration]

}
