package com.codacy.plugins.api

import com.codacy.plugins.api.results.Parameter
import play.api.libs.json.JsValue

final private[api] case class ParamValue(value: JsValue) extends AnyVal with Parameter.Value

final private[api] case class OptionsValue(value: JsValue) extends AnyVal with Options.Value
