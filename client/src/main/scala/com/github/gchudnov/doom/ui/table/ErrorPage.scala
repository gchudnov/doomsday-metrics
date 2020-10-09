package com.github.gchudnov.doom.ui.table

import com.github.gchudnov.doom.records.ErrorRecord
import com.github.gchudnov.swearwolf._
import com.github.gchudnov.swearwolf.util._

/**
 * A page with errors.
 *
 * Extract the latest error from the row and create a label out of it.
 * Next, prints the labels on the screen.
 */
final class ErrorPage(val id: Int, rows: List[Row], pos: Point) extends Page {

  override def render(screen: Screen): Either[Throwable, Unit] = {
    val size = screen.size

    val errRecords = rows
      .foldLeft(List.empty[ErrorRecord]) { (acc, row) =>
        row.history.lastErrorOption.fold(acc)(err => err +: acc)
      }
      .sortBy(_.at.getEpochSecond)
      .reverse
      .take(size.height)

    for {
      _ <- Func
             .sequence(errRecords.zipWithIndex.map({ case (err, i) =>
               val text = Text.maybeEllipsisRight(size.width)(formatText(i, err))
               screen.put(pos.offset(0, i), text)
             }))
             .map(_ => ())
    } yield ()
  }

  private def formatText(i: Int, err: ErrorRecord): String = {
    s"""${i}. ${err.id} @ ${err.at.toString}: ${err.t.getMessage}"""
  }

}
