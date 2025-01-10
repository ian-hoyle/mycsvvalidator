package validation.config

import ujson.Value
import upickle.core.LinkedHashMap
import validation.jsonschema.loadData

import scala.util.Try

object ConfigUtils:

  def loadProperties(file: String): LinkedHashMap[String, Value] = {
    val data = loadData(file)
    val json = ujson.read(data.getOrElse(""))
    val jsonMap: LinkedHashMap[String, Value] = json("properties").obj
    jsonMap
  }

  def getPropertyType(propertyValue: ujson.Obj): String = {
    propertyValue.obj.get("type") match {
      case Some(ujson.Str(singleType)) => singleType
      case Some(ujson.Arr(types)) if types.nonEmpty => types.head.str // TODO correctly handle arrays. Assuming string before null etc
      case _ => "unknown"
    }
  }

  def convertValueFunction(propertyType: String): String => Any = {
    propertyType match {
      case "integer" => (str:String) => Try(str.toInt).getOrElse(str)
      case "array" => (str: String) => if (str.isEmpty) "" else str.split("\\|")
      case "boolean" =>
        (str: String) =>
          str.toUpperCase match {
            case "YES" | "true" => true
            case "NO" | "false" => false
            case _ => str
          }
      case _ => (str:String) => str
    }
  }


