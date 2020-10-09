package com.github.gchudnov.doom.ui.table

import com.github.gchudnov.swearwolf.util.{ Point, Rect }
import com.github.gchudnov.swearwolf.woods._

object PageLayout {
  import PageLayoutState._

  def layout(rcArea: Rect, rows: List[Row], pageIndexStart: Int): List[Page] = {
    val pageWidth       = rcArea.width
    val pageHeight      = rcArea.height
    val dataRowsPerPage = pageHeight - 4 // 4 == table top frame + header + header separator + bottom frame

    if (dataRowsPerPage < 0 || rows.isEmpty) {
      List.empty[Page]
    } else {
      val valueRows   = rows.map(rowToData)
      val groupedRows = valueRows.grouped(dataRowsPerPage).toList

      val foldState = groupedRows.foldLeft(FoldState.empty) { (acc, set) =>
        val szTable = Table.estimateSize(set, TablePage.tableStyle)

        require(szTable.height <= pageHeight, s"table height is bigger than screen height: table-height = ${szTable.height}; rows-per-page: ${dataRowsPerPage}")

        if (acc.currentPage.estimatedWidth + szTable.width <= pageWidth) {
          val updCurrentPage = acc.currentPage.copy(rowSets = acc.currentPage.rowSets :+ RowSetState(set = set, estimatedWidth = szTable.width))
          acc.copy(currentPage = updCurrentPage)
        } else {
          val newCurrentPage = new PageState(rowSets = Vector(RowSetState(set = set, estimatedWidth = szTable.width)))
          acc.copy(completePages = acc.completePages :+ acc.currentPage, currentPage = newCurrentPage)
        }
      }

      val pageStates = foldState.completePages :+ foldState.currentPage

      val pages = pageStates.zipWithIndex.map({ case (pageState, i) =>
        val id = i + pageIndexStart
        val rowSets = pageState.rowSets.map(_.set)
        val offsets = 0 +: pageState.rowSets.map(_.estimatedWidth)

        val positions = pageState.rowSets.zip(offsets).zipWithIndex.map({
          case ((_, offset), j) =>
            Point(j * offset, rcArea.top)
        })

        Page.data(id = id, rowSets = rowSets, positions = positions)
      })

      pages.toList
    }
  }

  val headerRow: List[String] = List(
    Column.Name,
    Column.CurrentValue,
    Column.MinValue,
    Column.MaxValue,
    Column.History
  )

  private def rowToData(row: Row): List[String] = List(
    row.get(Column.Name),
    row.get(Column.CurrentValue),
    row.get(Column.MinValue),
    row.get(Column.MaxValue),
    row.get(Column.History)
  )

}

object PageLayoutState {

  final case class RowSetState(
    set: List[List[String]],
    estimatedWidth: Int
  )

  final case class PageState(
    rowSets: Vector[RowSetState]
  ) {
    val estimatedWidth: Int = rowSets.foldLeft(0)((acc, it) => acc + it.estimatedWidth)
  }

  object PageState {
    def empty: PageState = new PageState(rowSets = Vector.empty[RowSetState])
  }

  final case class FoldState(
    completePages: Vector[PageState],
    currentPage: PageState
  )

  object FoldState {
    def empty: FoldState = new FoldState(completePages = Vector.empty[PageState], currentPage = PageState.empty)
  }
}
