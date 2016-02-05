package codacy.dockerApi.utils

import codacy.dockerApi._

object ToolHelper {

  def getPatternsToLint(conf: Option[List[PatternDef]])(implicit spec: Spec): Option[List[PatternDef]] = {
    conf.map {
      configuration =>
        configuration.map(pattern => getMissingParametersFromSpec(pattern))
    }
  }

  private def buildParameterDefFromSpec(parameterSpec: ParameterSpec): ParameterDef = {
    ParameterDef(parameterSpec.name, parameterSpec.default)
  }

  private def getParametersByPatternId(patternId: PatternId)(implicit spec: Spec): Option[Set[ParameterDef]] = {
    val specPattern = spec.patterns.find(_.patternId == patternId)

    specPattern.flatMap {
      patternSpec =>
        patternSpec.parameters.map(params => params.map(buildParameterDefFromSpec))
    }
  }

  private def getMissingParametersFromSpec(pattern: PatternDef)(implicit spec: Spec): PatternDef = {
    val params = pattern.parameters.orElse(getParametersByPatternId(pattern.patternId))
    PatternDef(pattern.patternId, params)
  }

}
