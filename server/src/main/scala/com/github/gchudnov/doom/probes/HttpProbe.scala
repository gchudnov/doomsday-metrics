package com.github.gchudnov.doom.probes

import com.github.gchudnov.doom.queries.Query
import zio.duration.Duration

/**
 * Specifies the params used to execute the given http request.
 */
final case class HttpProbe(name: String, url: String, queries: Seq[Query], interval: Duration) extends Probe {}
