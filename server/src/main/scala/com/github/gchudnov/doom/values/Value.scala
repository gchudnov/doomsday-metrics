package com.github.gchudnov.doom.values

import com.github.gchudnov.doom.docs.{ CsvDoc, JsonDoc, TextDoc }
import zio.Task

trait Value {
  def kind: ValueType
}

object Value {

  def fromCsv(csvDoc: CsvDoc, csvPath: String, valueType: ValueType): Task[Value] =
    valueType match {
      case StringValueType =>
        csvDoc.string(csvPath).map(StringValue)
      case DoubleValueType =>
        csvDoc.double(csvPath).map(DoubleValue)
      case LongValueType =>
        csvDoc.long(csvPath).map(LongValue)
      case BooleanValueType =>
        csvDoc.boolean(csvPath).map(BooleanValue)
      case BigDecimalValueType =>
        csvDoc.bigDecimal(csvPath).map(BigDecimalValue)
      case DateTimeValueType =>
        csvDoc.dateTime(csvPath).map(DateTimeValue)
    }

  def fromJson(jsonDoc: JsonDoc, jsonPath: String, valueType: ValueType): Task[Value] =
    valueType match {
      case StringValueType =>
        jsonDoc.string(jsonPath).map(StringValue)
      case DoubleValueType =>
        jsonDoc.double(jsonPath).map(DoubleValue)
      case LongValueType =>
        jsonDoc.long(jsonPath).map(LongValue)
      case BooleanValueType =>
        jsonDoc.boolean(jsonPath).map(BooleanValue)
      case BigDecimalValueType =>
        jsonDoc.bigDecimal(jsonPath).map(BigDecimalValue)
      case DateTimeValueType =>
        jsonDoc.dateTime(jsonPath).map(DateTimeValue)
    }

  def fromText(textDoc: TextDoc, textPath: String, valueType: ValueType): Task[Value] =
    valueType match {
      case StringValueType =>
        textDoc.string(textPath).map(StringValue)
      case DoubleValueType =>
        textDoc.double(textPath).map(DoubleValue)
      case LongValueType =>
        textDoc.long(textPath).map(LongValue)
      case BooleanValueType =>
        textDoc.boolean(textPath).map(BooleanValue)
      case BigDecimalValueType =>
        textDoc.bigDecimal(textPath).map(BigDecimalValue)
      case DateTimeValueType =>
        textDoc.dateTime(textPath).map(DateTimeValue)
    }

}
