package com.github.gchudnov.doom.probes

import com.github.gchudnov.doom.queries.Query
import zio.duration.Duration

/**
 * Specifies the params to execute the given shell script.
 */
final case class ShellProbe(name: String, cmd: String, queries: Seq[Query], interval: Duration) extends Probe {}
