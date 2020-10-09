package com.github.gchudnov.doom.records

import java.time.Instant

import com.github.gchudnov.doom.probes.Probe
import com.github.gchudnov.doom.queries.Query
import com.github.gchudnov.doom.values.Value

/**
 * A value record from a Probe
 */
final case class ValueRecord(probe: Probe, query: Query, at: Instant, value: Value) extends Record {}
