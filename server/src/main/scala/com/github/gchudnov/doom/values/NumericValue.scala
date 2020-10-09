package com.github.gchudnov.doom.values

/**
 * Represents a numeric value
 */
trait NumericValue extends Value {
  def toInt: Int
  def toLong: Long
  def toFloat: Float
  def toDouble: Double
}
