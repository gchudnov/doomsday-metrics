package com.github.gchudnov.doom.values

final case class BooleanValue(value: Boolean) extends Value {
  override def kind: ValueType = BooleanValueType

  override def toString: String = value.toString
}
