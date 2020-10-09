package com.github.gchudnov.doom.records

import java.time.Instant

import com.github.gchudnov.doom.probes.Probe
import com.github.gchudnov.doom.queries.Query

/**
 * An entry returned by Probe
 */
trait Record {
  def probe: Probe
  def query: Query
  def at: Instant

  def id: String = probe.name + "/" + query.name
}

object Record {

  def toString(rec: Record): String = rec match {
    case ValueRecord(_, _, _, value) =>
      value.toString
    case ErrorRecord(_, _, _, t) =>
      t.toString
  }

}
