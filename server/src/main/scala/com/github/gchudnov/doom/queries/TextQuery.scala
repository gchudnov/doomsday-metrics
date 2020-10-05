package com.github.gchudnov.doom.queries

import com.github.gchudnov.doom.values.ValueType

/**
 * A text query to make against the returned data.
 * Used to return the value as-is.
 */
final case class TextQuery(name: String, textPath: String, valueType: ValueType) extends Query
