package com.github.gchudnov.doom.ui.table

import com.github.gchudnov.doom.records.history.RecordHistory
import com.github.gchudnov.doom.records.{ Record, ValueRecord }
import com.github.gchudnov.doom.values.NumericValue
import com.github.gchudnov.swearwolf.term.ArrayScreen
import com.github.gchudnov.swearwolf.util.{ Point, Size }
import com.github.gchudnov.swearwolf.woods._

final class NumericRow(val name: String, val history: RecordHistory) extends Row {
  import NumericRow._
  import Row._

  override def add(record: Record): Row =
    new NumericRow(name, history.add(record))

  override def get(colName: String): String = colName match {
    case Column.Name         => name
    case Column.CurrentValue => history.lastValueOption.map(it => valueToString(toDouble(it))).getOrElse(NoValue)
    case Column.MaxValue     => history.maxOption.map(it => valueToString(toDouble(it))).getOrElse(NoValue)
    case Column.MinValue     => history.minOption.map(it => valueToString(toDouble(it))).getOrElse(NoValue)
    case Column.History =>
      val screen = ArrayScreen(Size(history.capacity, 1), ' ', None)
      val values = history.records.map(recordToDouble)
      val g      = Graph(Size(Row.HistoryLen, 1), values, GraphStyle.Step)
      screen.put(Point(0, 0), g).fold(_ => "", _ => screen.toString)
  }
}

object NumericRow {

  private def valueToString(value: Double): String =
    f"${value}%.2f"

  private def toDoubleOption(valueRecord: ValueRecord): Option[Double] = valueRecord.value match {
    case n: NumericValue =>
      Some(n.toDouble)
    case _ =>
      None
  }

  private def toDouble(rec: ValueRecord): Double =
    toDoubleOption(rec).getOrElse(0.0)

  private def recordToDouble(rec: Record): Double = rec match {
    case valueRecord: ValueRecord =>
      toDouble(valueRecord)
    case _ =>
      0.0
  }
}
