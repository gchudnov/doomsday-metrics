package com.github.gchudnov.doom.ui.table

import com.github.gchudnov.doom.BuildInfo
import com.github.gchudnov.doom.records.Record
import com.github.gchudnov.doom.ui.Dashboard
import com.github.gchudnov.swearwolf._
import com.github.gchudnov.swearwolf.util._
import com.github.gchudnov.swearwolf.woods._

final class TableDashboard(lineMap: Map[String, Row], selectedPageId: Int) extends Dashboard {
  import TableDashboard._

  private val pos: Point  = Point(0, 0)

  override def update(records: Seq[Record]): Dashboard = {
    val updLineMap = records.foldLeft(lineMap)({ case (acc, record) =>
      val row    = acc.getOrElse(record.id, rowFromRecord(record))
      val updRow = row.add(record)
      acc.updated(record.id, updRow)
    })

    new TableDashboard(updLineMap, selectedPageId)
  }

  override def keyseq(keySeq: KeySeq): Dashboard = {
    val och = keySeq.char
    och.fold(this)(ch => {
      if(ch >= '0' && ch <= '9') {
        val newSelectedPageId = (ch - '0')
        new TableDashboard(lineMap, newSelectedPageId)
      } else {
        this
      }
    })
  }

  override def render(screen: Screen): Either[Throwable, Unit] = {
    val size = screen.size

    val headerValue = s"${BuildInfo.name} v${BuildInfo.version}"
    val headerLabel = Label(Size(size.width, 1), headerValue, AlignStyle.Left, isFill = true)

    val rows = lineMap.values.toList.sortBy(_.name)

    val rcArea = Rect(Point(0, 1), size.copy(height = size.height - 1)) // offset 1 for the header

    val errPage = Page.error(pageIndexError, rows, pos.offset(0, 1)) // error page had always index 0.
    val pages  = (errPage +: PageLayout.layout(rcArea, rows, pageIndexStart)).take(10)

    val selectedPage = pages.zipWithIndex.find(_._2 == selectedPageId).map(_._1)

    val pagerOffsetX = size.width - pages.size - 1

    for {
      pager <- makePager(pages.size, selectedPage.map(_.id).getOrElse(pageIndexError))
      _ <- screen.put(pos, headerLabel, TextStyle.Background(NamedColor.SlateGray))
      _ <- screen.put(pos.offset(pagerOffsetX, 0), pager)
      _ <- selectedPage.map(page => page.render(screen)).getOrElse(Right(()))
    } yield ()
  }
}

object TableDashboard {

  private val initSelectedPageId = 1

  private val pageIndexError = 0
  private val pageIndexStart = 1

  def empty: TableDashboard = new TableDashboard(lineMap = Map.empty[String, Row], selectedPageId = initSelectedPageId)

  private def rowFromRecord(record: Record): Row =
    Row.empty(name = record.id, kind = record.query.valueType)

  private[table] def makePager(nPages: Int, selected: Int): Either[Throwable, RichText] = {
    val str = Range(0, nPages).foldLeft(new StringBuilder)((acc, i) =>  {
      val p = if(i == selected) s"<b>${i}</b>" else s"${i}"
      acc.append(p)
    }).toString()

    val text = s"<color bg='slate-gray'>${str}</color>"

    RichText.make(text)
  }
}
