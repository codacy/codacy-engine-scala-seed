package codacy.docker.api.utils

import codacy.docker.api._

object ToolHelper {

  def filesToLint(source: Source.Directory, filesOpt: Option[Set[Source.File]]): Set[String] = {
    filesOpt.fold(Set(source.path)) { files =>
      files.map(_.path)
    }
  }

  def patternsToLint(conf: Option[List[Pattern.Definition]])(implicit specification: Tool.Specification): Option[List[Pattern.Definition]] = {
    conf.map { configuration =>
      configuration.map(pattern => getMissingParametersFromSpec(pattern))
    }
  }

  private def buildParameterDefFromSpec(parameterSpec: Parameter.Specification): Parameter.Definition = {
    Parameter.Definition(parameterSpec.name, parameterSpec.default)
  }

  private def getParametersByPatternId(patternId: Pattern.Id)(implicit specification: Tool.Specification): Option[Set[Parameter.Definition]] = {
    val specPattern = specification.patterns.find(_.patternId == patternId)

    specPattern.flatMap { patternSpec =>
      patternSpec.parameters.map(params => params.map(buildParameterDefFromSpec))
    }
  }

  private def getMissingParametersFromSpec(pattern: Pattern.Definition)(implicit specification: Tool.Specification): Pattern.Definition = {
    val params = pattern.parameters.orElse(getParametersByPatternId(pattern.patternId))
    Pattern.Definition(pattern.patternId, params)
  }

}
