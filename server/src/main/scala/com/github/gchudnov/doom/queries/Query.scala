package com.github.gchudnov.doom.queries

import com.github.gchudnov.doom.values.ValueType

/**
 * A query to make against the returned data.
 */
trait Query {
  def name: String
  def valueType: ValueType
}
