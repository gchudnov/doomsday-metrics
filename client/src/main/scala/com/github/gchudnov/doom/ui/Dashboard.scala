package com.github.gchudnov.doom.ui

import com.github.gchudnov.doom.records.Record
import com.github.gchudnov.doom.ui.table.TableDashboard
import com.github.gchudnov.swearwolf._

trait Dashboard {
  def update(records: Seq[Record]): Dashboard
  def keyseq(keySeq: KeySeq): Dashboard
  def render(screen: Screen): Either[Throwable, Unit]
}

object Dashboard {
  def empty: Dashboard = TableDashboard.empty
}
