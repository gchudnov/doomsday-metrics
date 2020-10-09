package com.github.gchudnov.doom.values

final case class StringValue(value: String) extends Value {
  override def kind: ValueType = StringValueType

  override def toString: String = value
}
