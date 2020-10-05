package com.github.gchudnov.doom.ui.table
import com.github.gchudnov.doom.records.Record
import com.github.gchudnov.doom.records.history.RecordHistory

final class StringRow(val name: String, val history: RecordHistory) extends Row {
  import Row._

  override def add(record: Record): Row =
    new StringRow(name, history.add(record))

  override def get(colName: String): String = colName match {
    case Column.Name         => name
    case Column.CurrentValue => history.lastValueOption.map(_.value.toString).getOrElse(NoValue)
    case _                   => NoValue
  }

}
