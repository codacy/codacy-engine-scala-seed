package com.codacy.tools.scala.seed.utils

import com.codacy.plugins.api.results.{IssuesTool, Parameter, Pattern}

object ToolHelper {

  implicit class ConfigurationOps(configuration: Option[List[Pattern.Definition]]) {

    def withDefaultParameters(implicit spec: IssuesTool.Specification): Option[List[Pattern.Definition]] = {
      configuration.map(
        _.map(pattern => pattern.copy(parameters = pattern.parameters.orElse(defaultParameters(pattern.patternId))))
      )
    }
  }

  implicit class ParameterSpecificationOps(parameterSpec: Parameter.Specification) {
    def toDefinition: Parameter.Definition = Parameter.Definition(parameterSpec.name, parameterSpec.default)
  }

  private def defaultParameters(
    patternId: Pattern.Id
  )(implicit spec: IssuesTool.Specification): Option[Set[Parameter.Definition]] = {
    spec.patterns
      .find(_.patternId == patternId)
      .flatMap { patternSpec => patternSpec.parameters.map(_.map(_.toDefinition))
      }
  }

}
