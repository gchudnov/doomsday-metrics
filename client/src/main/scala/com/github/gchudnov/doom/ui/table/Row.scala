package com.github.gchudnov.doom.ui.table

import com.github.gchudnov.doom.records.Record
import com.github.gchudnov.doom.records.history.RecordHistory
import com.github.gchudnov.doom.values.{ NumericValueType, ValueType }

trait Row {
  def name: String

  def history: RecordHistory

  def add(record: Record): Row
  def get(colName: String): String
}

object Row {

  val HistoryLen: Int  = 15
  val NoValue: String = "-"

  def empty(name: String, kind: ValueType): Row = {
    val history = RecordHistory.bounded(HistoryLen)
    kind match {
      case _: NumericValueType =>
        new NumericRow(name, history)
      case _ =>
        new StringRow(name, history)
    }
  }

}
