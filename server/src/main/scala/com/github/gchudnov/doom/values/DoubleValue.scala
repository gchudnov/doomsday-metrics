package com.github.gchudnov.doom.values

final case class DoubleValue(value: Double) extends NumericValue {
  override def kind: ValueType = DoubleValueType

  override def toString: String = value.toString
  override def toInt: Int       = value.toInt
  override def toLong: Long     = value.toLong
  override def toFloat: Float   = value.toFloat
  override def toDouble: Double = value
}
