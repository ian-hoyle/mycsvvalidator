package validation

import cats.data.Validated.*
import cats.data.{NonEmptyList, Reader, Validated}
import cats.effect.IO
import cats.implicits.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import config.ValidationConfig
import csv.CSVUtils.csvFileValidations
import csv.{CSVUtils, RowData}
import error.CSVValidationResult.combineCSVValidationResult
import error.ValidationErrors
import validation.jsonschema.ValidatedSchema
import validation.jsonschema.ValidatedSchema.CSVValidationResult


object JsonSchemaValidated:

  def prepareCSVConfiguration(parameters: Parameters): IO[CSVValidatorConfiguration] = {
    IO({
      val csvConfigurationReader = for {
        altHeaderToPropertyMapper <- Reader(ValidationConfig.alternateKeyToPropertyMapper)
        propertyToAltHeaderMapper <- Reader(ValidationConfig.propertyToAlternateKeyMapper)
        valueMapper <- Reader(ValidationConfig.valueMapper)
      } yield CSVValidatorConfiguration(altHeaderToPropertyMapper, propertyToAltHeaderMapper,
        valueMapper, parameters.fileToValidate, parameters.idKey,
        parameters.requiredSchema, parameters.schema)
      csvConfigurationReader.run(parameters)
    }
    ) //TODO handle error with raiseError that contains ValidationResult
  }

  def dataValidation(fileValidation: CSVValidationResult[List[RowData]], schema: List[String]): IO[CSVValidationResult[List[RowData]]] = {
    fileValidation match {
      case Valid(value) =>
        val schemaValidations = schema.map(x => ValidatedSchema.schemaValidated(Some(x)))
        val dataValidations = schemaValidations.map { validation =>
          IO(validation(value))
        }
        // add other data validations here to dataValidations
        dataValidations.parSequence.map(_.combineAll)
      case Invalid(errors) =>
        IO.pure(errors.invalid)
    }
  }

  def convertToJSONString(data: Map[String, Any], keyMapper: String => String, valueMapper: String => Any => Any): String = {
    val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
    val convertedData = data.map {
      case (header, value) =>
        val property = keyMapper(header)
        (property,
          if (value.toString.isEmpty) null
          else valueMapper(property)(value)
        )
    }
    val generatedJson = mapper.writeValueAsString(convertedData)
    generatedJson
  }


case class CSVValidatorConfiguration(altToProperty: String => String,
                                     propertyToAlt: String => String,
                                     valueMapper: (property:String) => Any => Any,
                                     csvFile: String,
                                     idKey: Option[String],
                                     requiredSchema: Option[String],
                                     schema: List[String]
                                    )
// Comes from arguments
case class Parameters(csConfig: String,
                      schema: List[String],
                      alternates: Option[String],
                      fileToValidate: String,
                      idKey: Option[String] = None,
                      requiredSchema: Option[String] = None)