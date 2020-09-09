package com.codacy.tools.scala.seed.utils

import com.codacy.plugins.api.results.{Parameter, Pattern, Tool}

object ToolHelper {

  implicit class ConfigurationOps(val configuration: Option[List[Pattern.Definition]]) extends AnyVal {

    def withDefaultParameters(implicit spec: Tool.Specification): Option[List[Pattern.Definition]] = {
      configuration.map(_.map { pattern =>
        val defaults = defaultParameters(pattern.patternId)
        val configurationParameters = pattern.parameters

        val parameters = defaults
          .filterNot(default => configurationParameters.exists(default.name == _.name)) ++ configurationParameters

        pattern.copy(parameters = parameters)
      })
    }
  }

  implicit class ParameterSpecificationOps(val parameterSpec: Parameter.Specification) extends AnyVal {
    def toDefinition: Parameter.Definition = Parameter.Definition(parameterSpec.name, parameterSpec.default)
  }

  private def defaultParameters(
    patternId: Pattern.Id
  )(implicit spec: Tool.Specification): Set[Parameter.Definition] = {
    spec.patterns
      .find(_.patternId == patternId)
      .map { patternSpec =>
        patternSpec.parameters.map(_.toDefinition)
      }
      .getOrElse(Set.empty)
  }

}
