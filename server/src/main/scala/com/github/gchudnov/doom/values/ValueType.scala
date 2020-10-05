package com.github.gchudnov.doom.values

sealed trait ValueType

sealed trait NumericValueType extends ValueType

case object StringValueType     extends ValueType
case object DoubleValueType     extends NumericValueType
case object LongValueType       extends NumericValueType
case object BooleanValueType    extends ValueType
case object BigDecimalValueType extends NumericValueType
case object DateTimeValueType   extends ValueType

final class ValueTypeException(value: String) extends RuntimeException(s"Cannot parse ValueType: '${value}'")

object ValueType {

  private val StringName     = "string"
  private val DoubleName     = "double"
  private val LongName       = "long"
  private val BooleanName    = "boolean"
  private val BigDecimalName = "big-decimal"
  private val DateTimeName   = "date-time"

  def apply(value: String): Either[Throwable, ValueType] =
    value.toLowerCase match {
      case StringName =>
        Right(StringValueType)

      case DoubleName =>
        Right(DoubleValueType)

      case LongName =>
        Right(LongValueType)

      case BooleanName =>
        Right(BooleanValueType)

      case BigDecimalName =>
        Right(BigDecimalValueType)

      case DateTimeName =>
        Right(DateTimeValueType)

      case _ =>
        Left(new ValueTypeException(value))
    }
}
