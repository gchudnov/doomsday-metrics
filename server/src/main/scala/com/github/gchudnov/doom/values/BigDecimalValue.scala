package com.github.gchudnov.doom.values

final case class BigDecimalValue(value: BigDecimal) extends NumericValue {
  override def kind: ValueType = BigDecimalValueType

  override def toString: String = value.toString
  override def toInt: Int       = value.toInt
  override def toLong: Long     = value.toLong
  override def toFloat: Float   = value.toFloat
  override def toDouble: Double = value.toDouble
}
