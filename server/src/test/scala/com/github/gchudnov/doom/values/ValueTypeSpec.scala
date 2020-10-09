package com.github.gchudnov.doom.values

import zio._
import zio.test.Assertion._
import zio.test._

object ValueTypeSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("ValueType")(
      testM("string can be parsed") {
        val input = "string"
        for {
          valueType <- ZIO.fromEither(ValueType(input))
        } yield assert(valueType)(equalTo(StringValueType))
      },
      testM("double can be parsed") {
        val input = "double"
        for {
          valueType <- ZIO.fromEither(ValueType(input))
        } yield assert(valueType)(equalTo(DoubleValueType))
      },
      testM("long can be parsed") {
        val input = "long"
        for {
          valueType <- ZIO.fromEither(ValueType(input))
        } yield assert(valueType)(equalTo(LongValueType))
      },
      testM("boolean can be parsed") {
        val input = "boolean"
        for {
          valueType <- ZIO.fromEither(ValueType(input))
        } yield assert(valueType)(equalTo(BooleanValueType))
      },
      testM("big-decimal can be parsed") {
        val input = "big-decimal"
        for {
          valueType <- ZIO.fromEither(ValueType(input))
        } yield assert(valueType)(equalTo(BigDecimalValueType))
      },
      testM("date-time can be parsed") {
        val input = "date-time"
        for {
          valueType <- ZIO.fromEither(ValueType(input))
        } yield assert(valueType)(equalTo(DateTimeValueType))
      },
      testM("parse is case insensitive") {
        val input = "DATE-time"
        for {
          valueType <- ZIO.fromEither(ValueType(input))
        } yield assert(valueType)(equalTo(DateTimeValueType))
      },
      testM("invalid value cannot be parsed") {
        val input = "invalid-data-type-name"
        for {
          valueType <- ZIO.fromEither(ValueType(input)).either
        } yield assert(valueType)(isLeft)
      }
    )
}
