package com.github.gchudnov.doom.records.history

import com.github.gchudnov.doom.records.{ ErrorRecord, Record, ValueRecord }

trait RecordHistory {
  def size: Int
  def capacity: Int

  def records: Vector[Record]

  def minOption: Option[ValueRecord]
  def maxOption: Option[ValueRecord]

  def lastValueOption: Option[ValueRecord]
  def lastErrorOption: Option[ErrorRecord]

  def add(record: Record): RecordHistory
}

object RecordHistory {

  def bounded(capacity: Int) =
    new BoundedRecordHistory(capacity = capacity, records = Vector.empty[Record])

}
