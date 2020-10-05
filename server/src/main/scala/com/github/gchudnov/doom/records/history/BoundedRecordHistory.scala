package com.github.gchudnov.doom.records.history

import com.github.gchudnov.doom.records.{ ErrorRecord, Record, ValueRecord }
import com.github.gchudnov.doom.values.NumericValue

/**
 * A Record history.
 * Newest records are appended on the right
 * @param capacity capacity of the history
 * @param records history points
 */
final class BoundedRecordHistory(val capacity: Int, val records: Vector[Record]) extends RecordHistory {
  import BoundedRecordHistory._

  override def size: Int = records.size

  override def minOption: Option[ValueRecord] =
    filterValueRecords
      .minByOption(toDouble)

  override def maxOption: Option[ValueRecord] =
    filterValueRecords
      .maxByOption(toDouble)

  override def lastValueOption: Option[ValueRecord] =
    records.lastOption.fold[Option[ValueRecord]](None) {
      case rec: ValueRecord => Some(rec)
      case _: ErrorRecord   => None
    }

  override def lastErrorOption: Option[ErrorRecord] =
    records.lastOption.fold[Option[ErrorRecord]](None) {
      case _: ValueRecord   => None
      case err: ErrorRecord => Some(err)
    }

  override def add(record: Record): RecordHistory = {
    val rs = (records :+ record).takeRight(capacity)
    new BoundedRecordHistory(capacity, rs)
  }

  private def filterValueRecords: Vector[ValueRecord] =
    records
      .collect({ case rec: ValueRecord => rec })

}

object BoundedRecordHistory {

  private def toDoubleOption(valueRecord: ValueRecord): Option[Double] = valueRecord.value match {
    case n: NumericValue =>
      Some(n.toDouble)
    case _ =>
      None
  }

  private def toDouble(rec: ValueRecord): Double =
    toDoubleOption(rec).getOrElse(0.0)

}
