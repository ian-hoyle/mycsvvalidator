package error

import cats.data.Validated.*
import cats.data.{NonEmptyList, Validated}
import cats.kernel.Monoid
import csv.RowData
import error.FileError.FileError
import validation.jsonschema.ValidatedSchema.CSVValidationResult

import java.text.SimpleDateFormat
import java.util.{Date, UUID}

object FileError extends Enumeration {
  type FileError = Value
  val UTF_8, INVALID_CSV, ROW_VALIDATION, SCHEMA_REQUIRED, DUPLICATE_HEADER, SCHEMA_VALIDATION, UNKNOWN, None = Value
}

case class Metadata(a: String)

case class Error(validationProcess: String, property: String, errorKey: String, message: String)

case class ValidationErrors(assetId: String, errors: Set[Error], data: List[Metadata] = List.empty[Metadata])

object ValidationErrors {
  implicit val combineValidationErrors: Monoid[List[ValidationErrors]] = new Monoid[List[ValidationErrors]]:
    override def empty: List[ValidationErrors] = List.empty[ValidationErrors]

    override def combine(validationErrors: List[ValidationErrors], moreValidationErrors: List[ValidationErrors]): List[ValidationErrors] =
      (validationErrors ++ moreValidationErrors)
        .groupBy(_.assetId)
        .map { case (id, validationErrors) =>
          ValidationErrors(
            assetId = id,
            errors = validationErrors.flatMap(_.errors).toSet,
            data = validationErrors.flatMap(_.data).distinct
          )
        }
        .toList
}

import cats.implicits.*

object CSVValidationResult {
  implicit val combineCSVValidationResult: Monoid[CSVValidationResult[List[RowData]]] = new Monoid[CSVValidationResult[List[RowData]]] {
    override def empty: CSVValidationResult[List[RowData]] =
      Validated.valid(List.empty[RowData]) // Empty list of RowData is the valid default

    override def combine( x: CSVValidationResult[List[RowData]], y: CSVValidationResult[List[RowData]] ): CSVValidationResult[List[RowData]] =
      (x, y) match {
        case (Valid(valueX), Valid(valueY)) =>
             Valid(valueY)
        case (Invalid(errorsX), Invalid(errorsY)) =>
          import ValidationErrors.combineValidationErrors
          Invalid(NonEmptyList.fromList(errorsX.toList |+| errorsY.toList).get)
        case (Valid(valueX), Invalid(errorsY)) =>
          Invalid(errorsY)
        case (Invalid(errorsX), Valid(valueY)) =>
          Invalid(errorsX)
      }
  }
}


case class ErrorFileData(consignmentId: UUID, date: String, fileError: FileError, validationErrors: List[ValidationErrors])

object ErrorFileData {

  def apply(fileError: FileError = FileError.None, validationErrors: List[ValidationErrors] = Nil): ErrorFileData = {

    val pattern = "yyyy-MM-dd"
    val dateFormat = new SimpleDateFormat(pattern)
    ErrorFileData(UUID.randomUUID(), dateFormat.format(new Date), fileError, validationErrors)
  }
}
