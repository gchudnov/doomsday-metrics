package com.github.gchudnov.doom.records

import java.time.Instant

import com.github.gchudnov.doom.probes.Probe
import com.github.gchudnov.doom.queries.Query

/**
 * Represent a record from the probe
 */
final case class ErrorRecord(probe: Probe, query: Query, at: Instant, t: Throwable) extends Record {}
