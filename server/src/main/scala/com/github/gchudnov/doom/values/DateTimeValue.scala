package com.github.gchudnov.doom.values

import java.time.OffsetDateTime

final case class DateTimeValue(value: OffsetDateTime) extends Value {
  override def kind: ValueType = DateTimeValueType

  override def toString: String = value.toString
}
