package com.github.gchudnov.doom.queries

import com.github.gchudnov.doom.values.ValueType

/**
 * A csv query to make against the returned data.
 *
 * @param columnName name of the column to return.
 */
final case class CsvQuery(name: String, csvPath: String, separator: String, valueType: ValueType) extends Query
