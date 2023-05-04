package com.codacy.plugins.api

import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import play.api.libs.json.Json

class FormatSpecs extends munit.FunSuite {
  test("allow a pattern specification with no `enabled`, `parameters` nor `languages`") {
    val patternId = "a-pattern-id"
    val level = Result.Level.Info
    val category = Pattern.Category.CodeStyle

    val json = s"""{"patternId": "$patternId", "level": "$level", "category": "$category"}"""

    val result = Json.parse(json).as[Pattern.Specification]
    val expectedResult =
      Pattern.Specification(Pattern.Id(patternId), level, category, None, None, Set.empty, Set.empty, false)

    assertEquals(result, expectedResult)
  }
  test("allow a pattern definition with no `parameters` in pattern objects in `patterns`") {
    val name = "a-tool"
    val patternId = "a-pattern-id"
    val file = "a-file"

    val json =
      s"""{
           |  "tools": [
           |    {
           |      "name": "$name",
           |      "patterns": [
           |        {
           |          "patternId": "$patternId"
           |        }
           |      ]
           |    }
           |  ],
           |  "files": ["$file"]
           |}""".stripMargin

    val result = Json.parse(json).as[Tool.CodacyConfiguration]
    val expectedResult =
      Tool.CodacyConfiguration(
        Set(Tool.Configuration(Tool.Name(name), Some(List(Pattern.Definition(Pattern.Id(patternId)))))),
        files = Some(Set(Source.File(file))),
        options = None
      )

    assertEquals(result, expectedResult)
  }
}
