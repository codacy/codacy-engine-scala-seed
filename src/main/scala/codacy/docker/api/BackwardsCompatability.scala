package codacy.docker.api

import java.nio.file.Paths

import codacy.docker.api.JsonApi._
import codacy.dockerApi
import codacy.dockerApi.{ParameterDef, ParameterName, ParameterSpec, PatternDef, PatternId, PatternSpec, Spec, ToolName}
import play.api.libs.json._

import scala.util.Try

trait BackwardsCompatability {

  implicit class AsTool(tool: dockerApi.Tool) extends Tool {
    override def apply(source: Source.Directory, configuration: Option[List[Pattern.Definition]],
                       files: Option[Set[Source.File]])(implicit specification: Tool.Specification): Try[List[Result]] = {
      tool.apply(
        path = Paths.get(source.path),
        conf = configuration.map(_.map(toPatternDef)),
        files = files.map(_.map(file => Paths.get(file.path)))
      )(toSpec(specification)).map(_.map(toResult))
    }
  }

  private def toResult(result: dockerApi.Result) = {
    result match {
      case dockerApi.Issue(filename, message, patternId, line) => Result.Issue(
        Source.File(filename.value), Result.Message(message.value),
        Pattern.Id(patternId.value), Source.Line(line.value)
      )
      case dockerApi.FileError(filename, messageOpt) =>
        Result.FileError(Source.File(filename.value), messageOpt.map(v => ErrorMessage(v.value)))
    }
  }

  private def toSpec(specification: Tool.Specification): Spec = {
    Spec(ToolName(specification.name.value), specification.patterns.map(toPatternSpec))
  }

  private def toPatternSpec(specification: Pattern.Specification): PatternSpec = {
    PatternSpec(PatternId(specification.patternId.value), specification.parameters.map(_.map(toParameterSpec)))
  }

  private def toParameterSpec(specification: Parameter.Specification): ParameterSpec = {
    ParameterSpec(ParameterName(specification.name.value), Json.toJson(specification.default))
  }


  private def toParameterDef(definition: Parameter.Definition): ParameterDef = {
    ParameterDef(ParameterName(definition.name.value), Json.toJson(definition.value))
  }

  private def toPatternDef(definition: Pattern.Definition): PatternDef = {
    PatternDef(PatternId(definition.patternId.value), definition.parameters.map(_.map(toParameterDef)))
  }
}
