package com.github.gchudnov.doom.values

final case class LongValue(value: Long) extends NumericValue {
  override def kind: ValueType = LongValueType

  override def toString: String = value.toString
  override def toInt: Int       = value.toInt
  override def toLong: Long     = value
  override def toFloat: Float   = value.toFloat
  override def toDouble: Double = value.toDouble
}
