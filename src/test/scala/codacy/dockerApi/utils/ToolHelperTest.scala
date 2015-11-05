package codacy.dockerApi.utils

import codacy.dockerApi._
import org.scalatest._
import play.api.libs.json.{JsNumber, JsString}

class ToolHelperTest extends FlatSpec with Matchers {

  val paramSpec1 = ParameterSpec(ParameterName("param1"), JsNumber(2))
  val paramSpec2 = ParameterSpec(ParameterName("param2"), JsString("value2"))
  val patternSpec1 = PatternSpec(PatternId("id1"), Some(Set(paramSpec1, paramSpec2)))
  val patternSpec2 = PatternSpec(PatternId("id2"), None)
  val patternsSpec = Set(patternSpec1, patternSpec2)

  val paramDef1 = ParameterDef(ParameterName("param1"), JsNumber(33))
  val paramDef2 = ParameterDef(ParameterName("param2"), JsString("value33"))
  val patternDef1 = PatternDef(PatternId("id1"), Some(Set(paramDef1, paramDef2)))
  val patternDef2 = PatternDef(PatternId("id2"), None)

  val patternDef1NoParam = PatternDef(PatternId("id1"), None)
  val patternDef2NoParam = PatternDef(PatternId("id2"), None)

  val genericSpec = codacy.dockerApi.Spec(ToolName("tool1"), patternsSpec)
  val genericConf: Option[Seq[PatternDef]] = Some(Seq(patternDef1, patternDef2))
  val genericConfNoParam: Option[Seq[PatternDef]] = Some(Seq(patternDef1NoParam, patternDef2NoParam))

  "ToolHelper" should "getPatternsFromConf" in {
    val spec = genericSpec
    val conf = genericConf

    val result = ToolHelper.getPatternsToLint(conf)(spec)

    result should equal(conf)
  }

  "ToolHelper" should "getPatternsNoneIfNoConf" in {
    val spec = genericSpec
    val conf = None

    val result = ToolHelper.getPatternsToLint(conf)(spec)

    result should equal(None)
  }

  "ToolHelper" should "getPatternsEmptyIfEmptyConf" in {
    val spec = genericSpec
    val conf: Option[Seq[PatternDef]] = Some(Seq())

    val result = ToolHelper.getPatternsToLint(conf)(spec)

    result should equal(Some(Seq()))
  }

  "ToolHelper" should "getParametersFromSpec" in {
    val spec = genericSpec
    val conf = genericConfNoParam

    val expectedParamDef1 = ParameterDef(ParameterName("param1"), JsNumber(2))
    val expectedParamDef2 = ParameterDef(ParameterName("param2"), JsString("value2"))
    val expectedPatternDef1 = PatternDef(PatternId("id1"), Some(Set(expectedParamDef1, expectedParamDef2)))
    val expectedPatternDef2 = PatternDef(PatternId("id2"), None)
    val expectedResult = Some(Seq(expectedPatternDef1, expectedPatternDef2))

    val result = ToolHelper.getPatternsToLint(conf)(spec)

    result should equal(expectedResult)
  }
}