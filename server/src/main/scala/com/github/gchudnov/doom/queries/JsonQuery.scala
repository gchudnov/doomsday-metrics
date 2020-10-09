package com.github.gchudnov.doom.queries

import com.github.gchudnov.doom.values.ValueType

final case class JsonQuery(name: String, jsonPath: String, valueType: ValueType) extends Query
