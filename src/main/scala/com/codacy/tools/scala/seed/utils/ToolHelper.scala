package com.codacy.tools.scala.seed.utils

import com.codacy.plugins.api.results.{Parameter, Pattern, Tool}

object ToolHelper {

  implicit class ConfigurationOps(configuration: Option[List[Pattern.Definition]]) {

    def withDefaultParameters(implicit spec: Tool.Specification): Option[List[Pattern.Definition]] = {
      configuration.map(_.map { pattern =>
        def toMap(setOpt: Option[Set[Parameter.Definition]]): Option[Map[Parameter.Name, Parameter.Definition]] =
          setOpt.map(paramSet => paramSet.map(p => (p.name, p))(collection.breakOut))

        val configurationParameters = toMap(pattern.parameters)
        val defaults = toMap(defaultParameters(pattern.patternId))

        val parameters =
          configurationParameters
            .map(defaults.getOrElse(Map.empty) ++ _) // we give priority to the configuration values over the default values
            .orElse(defaults)
            .map(_.values.toSet)

        pattern.copy(parameters = parameters)
      })
    }
  }

  implicit class ParameterSpecificationOps(parameterSpec: Parameter.Specification) {
    def toDefinition: Parameter.Definition = Parameter.Definition(parameterSpec.name, parameterSpec.default)
  }

  private def defaultParameters(
    patternId: Pattern.Id
  )(implicit spec: Tool.Specification): Option[Set[Parameter.Definition]] = {
    spec.patterns
      .find(_.patternId == patternId)
      .flatMap { patternSpec =>
        patternSpec.parameters.map(_.map(_.toDefinition))
      }
  }

}
