package config

import org.scalatest.funsuite.AnyFunSuite
import validation.Parameters

class ConfigTest extends AnyFunSuite:
  test("Load config from Resources") {
    val jsonConfigFileName = "https://raw.githubusercontent.com/nationalarchives/da-metadata-schema/main/metadata-schema/baseSchema.schema.json"
    val jsonConfigFileResources = "DaBase.json"
    val altKey = "tdrFileHeader"
    val idKey = "File path"
    val params = Parameters(jsonConfigFileResources, List.empty[String], Some(altKey), "sample.csv", Some(idKey))
    
    val propertyToAlternateKey = CSVParserConfig.propertyToAlternateKeyMapper(params)
    assert(propertyToAlternateKey("date_last_modified") == "Date last modified")

    val alternateKeyToProperty = CSVParserConfig.alternateKeyToPropertyMapper(params)

    assert(alternateKeyToProperty("Date last modified") == "date_last_modified")

    val propertyValueConvertor = CSVParserConfig.csvStringToValueMapper(params)

    assert(propertyValueConvertor("description_closed")("YES") == true)

 }
  test("Load config from URL") {
    val jsonConfigFileName = "https://raw.githubusercontent.com/nationalarchives/da-metadata-schema/main/metadata-schema/baseSchema.schema.json"
    val altKey = "tdrFileHeader"
    val params = Parameters(jsonConfigFileName, List.empty[String], Some(altKey), "sample.csv", Some("Filepath"))

    val propertyToAlternateKey = CSVParserConfig.propertyToAlternateKeyMapper(params)
    assert(propertyToAlternateKey("date_last_modified") == "Date last modified")

    val alternateKeyToProperty = CSVParserConfig.alternateKeyToPropertyMapper(params)

    assert(alternateKeyToProperty("Date last modified") == "date_last_modified")

    val propertyValueConvertor = CSVParserConfig.csvStringToValueMapper(params)

    assert(propertyValueConvertor("description_closed")("YES") == true)

  }

  test("Invalid alternate key returns original value") {
    val jsonConfigFileName = "https://raw.githubusercontent.com/nationalarchives/da-metadata-schema/main/metadata-schema/baseSchema.schema.json"
    val altKey = "badKey"
    val params = Parameters(jsonConfigFileName, List.empty[String], Some(altKey), "sample.csv", Some("Filepath"))

    val propertyToAlternateKey = CSVParserConfig.propertyToAlternateKeyMapper(params)
    assert(propertyToAlternateKey("date_last_modified") == "date_last_modified")

    val alternateKeyToProperty = CSVParserConfig.alternateKeyToPropertyMapper(params)

    assert(alternateKeyToProperty("Date last modified") == "Date last modified")

    val propertyValueConvertor = CSVParserConfig.csvStringToValueMapper(params)

    assert(propertyValueConvertor("description_closed")("YES") == true)

  }
