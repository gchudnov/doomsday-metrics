package com.github.gchudnov.doom.ui.table

import com.github.gchudnov.swearwolf.Screen
import com.github.gchudnov.swearwolf.util._
import com.github.gchudnov.swearwolf.woods._

final class TablePage(val id: Int, rowSets: Seq[List[List[String]]], positions: Seq[Point]) extends Page {
  import TablePage._

  override def render(screen: Screen): Either[Throwable, Unit] = {
    val tables = rowSets.map(set => Table(PageLayout.headerRow +: set, tableStyle))

    for {
      _ <- Func.sequence(tables.zip(positions).map({ case (table, pos) => screen.put(pos, table) }))
    } yield ()
  }

}

object TablePage {
  val tableStyle: TableStyle = TableStyle.Frame

}
