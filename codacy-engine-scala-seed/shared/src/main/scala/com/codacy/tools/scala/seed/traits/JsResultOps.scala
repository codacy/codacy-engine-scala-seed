package com.codacy.tools.scala.seed.traits

import play.api.libs.json.{JsError, JsResult, Json}

import scala.util.{Failure, Success, Try}

object JsResultOps {
  implicit class JsResultOps[A](private val result: JsResult[A]) extends AnyVal {

    def asTry: Try[A] =
      result.fold(error => Failure(new Throwable(Json.stringify(JsError.toJson(error)))), Success.apply)
  }
}
