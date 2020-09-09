package com.codacy.plugins.api

import com.codacy.plugins.api.results.{Pattern, Result}
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class FormatSpecs extends Specification {
  "Format" >> {
    "allow a pattern specification with no `enabled`, `parameters` nor `languages`" >> {
      val patternId = "a-pattern-id"
      val level = Result.Level.Info
      val category = Pattern.Category.CodeStyle

      val json = s"""{"patternId": "$patternId", "level": "$level", "category": "$category"}"""
      
      val result = Json.parse(json).as[Pattern.Specification]
      val expectedResult = Pattern.Specification(Pattern.Id(patternId), level, category, None, Set.empty, Set.empty, false)

      result shouldEqual expectedResult
    }
  }
}
