package codacy.docker

import play.api.libs.json.{JsString, JsValue, Json}

import scala.util.Try

package object api extends JsonApi{

  implicit class ParameterExtensions(param:Parameter.type){
    def Value(jsValue:JsValue):Parameter.Value = ParamValue(jsValue)
    def Value(raw:String):Parameter.Value = Value(Try(Json.parse(raw)).getOrElse(JsString(raw)))
  }

  implicit class ConfigurationExtensions(config:Configuration.type){
    def Value(jsValue:JsValue):Configuration.Value = ConfigurationValue(jsValue)
    def Value(raw:String):Configuration.Value = Value(Try(Json.parse(raw)).getOrElse(JsString(raw)))
  }

}
