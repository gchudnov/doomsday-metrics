package com.github.gchudnov.doom.probes

import java.time.Instant

/**
 * A result of probe execution.
 */
sealed trait ProbeResponse {
  def probe: Probe
  def at: Instant
}

/**
 * Probe result that returned an error
 */
final case class ProbeErrorResponse(probe: Probe, at: Instant, err: Throwable) extends ProbeResponse {}

/**
 * Probe Result that returned some value.
 */
final case class ProbeValueResponse(probe: Probe, at: Instant, value: String) extends ProbeResponse {}
