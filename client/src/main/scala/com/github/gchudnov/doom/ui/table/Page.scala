package com.github.gchudnov.doom.ui.table

import com.github.gchudnov.swearwolf.Screen
import com.github.gchudnov.swearwolf.util.Point

trait Page {
  def id: Int

  def render(screen: Screen): Either[Throwable, Unit]
}

object Page {

  def data(id: Int, rowSets: Seq[List[List[String]]], positions: Seq[Point]): Page =
    new TablePage(id, rowSets, positions)

  def error(id: Int, rows: List[Row], pos: Point): Page =
    new ErrorPage(id, rows, pos)

}
