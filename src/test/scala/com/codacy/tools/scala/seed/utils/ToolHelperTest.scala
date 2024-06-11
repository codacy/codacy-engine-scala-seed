package com.codacy.tools.scala.seed.utils

import com.codacy.plugins.api._
import com.codacy.plugins.api.results.{Parameter, Pattern, Result, Tool}
import ToolHelper._
import org.specs2.mutable.Specification
import play.api.libs.json.{JsNumber, JsString}

class ToolHelperTest extends Specification {

  val paramSpec1 = Parameter.Specification(Parameter.Name("param1"), Parameter.Value(JsNumber(2)))
  val paramSpec2 = Parameter.Specification(Parameter.Name("param2"), Parameter.Value(JsString("value2")))

  val patternSpecification1 = Pattern.Specification(Pattern.Id("id1"),
                                                    Result.Level.Warn,
                                                    Pattern.Category.CodeStyle,
                                                    None,
                                                    None,
                                                    Set(paramSpec1, paramSpec2))

  val patternSpecification2 =
    Pattern.Specification(Pattern.Id("id2"), Result.Level.Warn, Pattern.Category.CodeStyle, None, None, Set.empty)
  val patternsSpec = Set(patternSpecification1, patternSpecification2)

  val paramDef1 = Parameter.Definition(Parameter.Name("param1"), Parameter.Value(JsNumber(33)))
  val paramDef2 = Parameter.Definition(Parameter.Name("param2"), Parameter.Value(JsString("value33")))
  val patternDef1 = Pattern.Definition(Pattern.Id("id1"), Set(paramDef1, paramDef2))
  val patternDef2 = Pattern.Definition(Pattern.Id("id2"), Set.empty)

  val patternDef1NoParam = Pattern.Definition(Pattern.Id("id1"), Set.empty)
  val patternDef2NoParam = Pattern.Definition(Pattern.Id("id2"), Set.empty)

  val genericSpec = Tool.Specification(Tool.Name("tool1"), Option(Tool.Version("0.15.6")), patternsSpec)
  val genericConf: Option[List[Pattern.Definition]] = Some(List(patternDef1, patternDef2))
  val genericConfNoParam: Option[List[Pattern.Definition]] = Some(List(patternDef1NoParam, patternDef2NoParam))

  "ToolHelper" >> {
    "getPatternsFromConf" >> {
      val spec = genericSpec
      val conf = genericConf

      val result = conf.withDefaultParameters(spec)

      result must beEqualTo(conf)
    }

    "getPatternsNoneIfNoConf" >> {
      val spec = genericSpec
      val conf = None

      val result = conf.withDefaultParameters(spec)

      result must beNone
    }

    "getPatternsEmptyIfEmptyConf" >> {
      val spec = genericSpec
      val conf: Option[List[Pattern.Definition]] = Some(List())

      val result = conf.withDefaultParameters(spec)

      result must beEqualTo(Some(List()))
    }

    "getParametersFromSpec" >> {
      val spec = genericSpec
      val conf = genericConfNoParam

      val expectedParamDef1 = Parameter.Definition(Parameter.Name("param1"), Parameter.Value(JsNumber(2)))
      val expectedParamDef2 = Parameter.Definition(Parameter.Name("param2"), Parameter.Value(JsString("value2")))
      val expectedPatternDef1 = Pattern.Definition(Pattern.Id("id1"), Set(expectedParamDef1, expectedParamDef2))
      val expectedPatternDef2 = Pattern.Definition(Pattern.Id("id2"), Set.empty)
      val expectedResult = Some(List(expectedPatternDef1, expectedPatternDef2))

      val result = conf.withDefaultParameters(spec)

      result must beEqualTo(expectedResult)
    }

    "set the values of parameters not defined in the configuration to the default" >> {
      val spec = genericSpec.copy(patterns = Set(patternSpecification1))

      val confPattern = patternDef1.copy(parameters = Set(paramDef1))
      val conf = Some(List(confPattern))

      val result = conf.withDefaultParameters(spec)

      result must beEqualTo(
        Some(List(confPattern.copy(parameters = Set(paramDef1, paramDef2.copy(value = paramSpec2.default)))))
      )
    }
  }
}
