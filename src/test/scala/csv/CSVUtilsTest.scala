package csv

import org.scalatest.funsuite.AnyFunSuite
import validation.{CSVValidatorConfiguration, CSVValidator, Parameters}

class CSVUtilsTest extends AnyFunSuite:
  test("Load schema config") {
    val jsonConfigFileName = "https://raw.githubusercontent.com/nationalarchives/da-metadata-schema/main/metadata-schema/baseSchema.schema.json"
    val altKey = "tdrFileHeader"
    val idKey = "Filepath"
    val params = Parameters(jsonConfigFileName, List.empty[String], Some(altKey), "sample.csv", Some(idKey))
    import cats.effect.unsafe.implicits.global
    val csvValidationConfig: CSVValidatorConfiguration = CSVValidator.prepareCSVConfiguration(params).unsafeRunSync()
    assert(csvValidationConfig.valueMapper("description")("desc") == "desc")

  }
