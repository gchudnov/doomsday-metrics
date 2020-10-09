package com.github.gchudnov.doom.probes

import com.github.gchudnov.doom.queries.Query
import zio.duration.Duration

/**
 * A probe to run.
 */
trait Probe {
  def name: String
  def queries: Seq[Query]
  def interval: Duration
}
