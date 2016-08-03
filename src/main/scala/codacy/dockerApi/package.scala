package codacy

import java.nio.file.Path

import codacy.docker.api.{BackwardsCompatability, JsonApi}
import play.api.libs.json.{JsValue, Reads, Writes, _}

import scala.language.reflectiveCalls
import scala.util.{Failure, Success, Try}

package dockerApi {

  abstract class Formats[W <: AnyVal {val value : B}, B](apply_ : (B => W)) extends (B => W) {
    self =>

    implicit def writes(implicit writes: Writes[B]): Writes[W] = Writes(
      (_: W).value match { case value: B@unchecked => writes.writes(value) }
    )

    implicit def reads(implicit reads: Reads[B]): Reads[W] = reads.map(self.apply)

    override def apply(v1: B): W = apply_(v1)
  }

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  trait Tool {
    def apply(path: Path, conf: Option[List[PatternDef]], files: Option[Set[Path]])(implicit spec: Spec): Try[List[Result]]
  }

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final class PatternId(val value: String) extends AnyVal {
    override def toString = value.toString
  }

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final class SourcePath(val value: String) extends AnyVal {
    override def toString = value.toString
  }

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final class ResultMessage(val value: String) extends AnyVal {
    override def toString = value.toString
  }

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final class ResultLine(val value: Int) extends AnyVal {
    override def toString = value.toString
  }

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final class ToolName(val value: String) extends AnyVal {
    override def toString = value.toString
  }

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final class ErrorMessage(val value: String) extends AnyVal {
    override def toString = value.toString
  }

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final class ParameterName(val value: String) extends AnyVal {
    override def toString = value.toString
  }

  object PatternId extends Formats[PatternId, String](new PatternId(_))

  object SourcePath extends Formats[SourcePath, String](new SourcePath(_))

  object ResultMessage extends Formats[ResultMessage, String](new ResultMessage(_))

  object ResultLine extends Formats[ResultLine, Int](new ResultLine(_))

  object ToolName extends Formats[ToolName, String](new ToolName(_))

  object ErrorMessage extends Formats[ErrorMessage, String](new ErrorMessage(_))

  object ParameterName extends Formats[ParameterName, String](new ParameterName(_))

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  case class ParameterDef(name: ParameterName, value: JsValue)

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  case class PatternDef(patternId: PatternId, parameters: Option[Set[ParameterDef]])

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  case class ToolConfig(name: ToolName, patterns: List[PatternDef])

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  case class FullConfig(tools: Set[ToolConfig], files: Option[Set[SourcePath]])

  //there are other fields like name and description but i don't care about them inside the tool
  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  case class ParameterSpec(name: ParameterName, default: JsValue)

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  case class PatternSpec(patternId: PatternId, parameters: Option[Set[ParameterSpec]])

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  case class Spec(name: ToolName, patterns: Set[PatternSpec])

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  sealed trait Result

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final case class Issue(filename: SourcePath, message: ResultMessage, patternId: PatternId, line: ResultLine) extends Result

  @deprecated("use the new codacy.docker.api types instead", "2.7.0")
  final case class FileError(filename: SourcePath, message: Option[ErrorMessage]) extends Result

}

package object dockerApi extends BackwardsCompatability with JsonApi {

  implicit class ResultExtension[A](val result: JsResult[A]) extends AnyVal {
    def asTry = result.fold(
      error => Failure(new Throwable(Json.stringify(JsError.toJson(error)))),
      Success.apply
    )
  }

  implicit def toValue[A] = (a: AnyVal {def value: A}) => a.value

  implicit lazy val specReader: Reads[Spec] = {
    implicit val r1 = Json.reads[ParameterSpec]
    implicit val r0 = Json.reads[PatternSpec]
    Json.reads[Spec]
  }

  implicit def configReader(implicit spec: Spec): Reads[FullConfig] = {
    implicit val r1 = Json.reads[ParameterDef]
    implicit val r0 = Json.reads[PatternDef]

    implicit val r2 = Reads.set(Json.reads[ToolConfig])

    Json.reads[FullConfig]
  }

  implicit lazy val writer: Writes[Result] = {
    lazy val issueWrites = Json.writes[Issue]
    lazy val errorWrites = Json.writes[FileError]

    Writes[Result] { (result: Result) =>
      val base = result match {
        case issue: Issue => issueWrites.writes(issue)
        case error: FileError => errorWrites.writes(error)
      }

      (base, result.getClass.getTypeName.split('.').lastOption) match {
        case (o: JsObject, Some(tpe)) => o ++ Json.obj("type" -> tpe)
        case other => base
      }
    }
  }
}
