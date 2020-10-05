package com.github.gchudnov.doom.docs

import java.time.OffsetDateTime

import com.github.gchudnov.doom.util.Strings
import zio._

import scala.util.matching.Regex

final class CsvDocException(message: String, t: Throwable) extends RuntimeException(message, t) {
  def this(message: String) = {
    this(message, null)
  }

  override def equals(other: Any): Boolean =
    other match {
      case that: CsvDocException =>
        this.getMessage == that.getMessage
      case _ => false
    }
}

/**
 * CSV Document
 *
 * in query parameters, 'path' is a csv-path of the value to fetch.
 *
 * examples of csv-queries:
 *   name=123;value
 *                    1) select a column C1 that named 'name'.
 *                    2) inside of the column C1 find a row R where value cell-value equal to '123'.
 *                    3) select a column C2 that named 'value' and fetch the cell-value on row R.
 */
final case class CsvDoc(m: Map[String, List[String]]) extends Doc {
  import CsvDoc._

  def string(path: String): Task[String] =
    value(path)

  def double(path: String): Task[Double] =
    value(path).flatMap(it => Task(it.toDouble).mapError(t => new CsvDocException(s"cannot convert '${it}' value to double", t)))

  def long(path: String): Task[Long] =
    string(path).flatMap(it => Task(it.toLong).mapError(t => new CsvDocException(s"cannot convert '${it}' value to long", t)))

  def boolean(path: String): Task[Boolean] =
    string(path).flatMap(it => Task(it.toBoolean).mapError(t => new CsvDocException(s"cannot convert '${it}' value to boolean", t)))

  def bigDecimal(path: String): Task[BigDecimal] =
    string(path).flatMap(it => Task(BigDecimal(it)).mapError(t => new CsvDocException(s"cannot convert '${it}' value to big-decimal", t)))

  def dateTime(path: String): Task[OffsetDateTime] =
    string(path).flatMap(it => Task(OffsetDateTime.parse(it)).mapError(t => new CsvDocException(s"cannot convert '${it}' value to offset-date-time", t)))

  def value(path: String): Task[String] =
    for {
      cp  <- csvPath(path)
      sel <- ZIO.fromOption(m.get(cp.selector)).mapError(_ => new CsvDocException(s"selector column '${cp.selector}' not found."))
      row <- ZIO
               .fromOption(sel.zipWithIndex.find(_._1 == cp.value).map(_._2))
               .mapError(_ => new CsvDocException(s"cell with value '${cp.value}' not found inside of the selector column: '${cp.selector}'."))
      col   <- ZIO.fromOption(m.get(cp.column)).mapError(_ => new CsvDocException(s"value column '${cp.column}' not found."))
      value <- ZIO.fromOption(col.zipWithIndex.find(_._2 == row).map(_._1)).mapError(_ => new CsvDocException(s"value row with index '${row}' not found."))
    } yield value
}

object CsvDoc {

  final case class CsvPath(selector: String, value: String, column: String)

  object RxGroups {
    val Selector = "sel"
    val Value    = "val"
    val Column   = "col"
  }

  private val groupNames = Seq(RxGroups.Selector, RxGroups.Value, RxGroups.Column)
  private val pattern    = new Regex("""^(?<sel>.+)\s*=\s*(?<val>.+)\s*;\s*(?<col>.+)$""", groupNames: _*)

  def parse(value: String, opts: CsvOpts): Task[CsvDoc] =
    Task {
      val data = value
        .split("\n")
        .map(_.split(opts.separator).map(_.trim))
        .filter(_.nonEmpty)

      val header       = data.head
      val initialState = header.map(k => (k -> List.empty[String])).toMap

      val m = data.tail.foldLeft(initialState) { (acc, line) =>
        val pairs = line.zipWithIndex.map { case (value, i) =>
          val key = header(i)
          (key -> value)
        }

        pairs.foldLeft(acc) { case (acc, (k, v)) =>
          acc + (k -> (acc(k) ++ List(v)))
        }
      }

      CsvDoc(m)
    }

  private[docs] def csvPath(path: String): Task[CsvPath] =
    for {
      m        <- ZIO.fromOption(pattern.findFirstMatchIn(path.trim)).mapError(_ => new CsvDocException(s"csv-path '${path.trim}' cannot be parsed."))
      selector <- Task(m.group(RxGroups.Selector))
      value    <- Task(m.group(RxGroups.Value))
      column   <- Task(m.group(RxGroups.Column))
    } yield CsvPath(selector = Strings.unquote(selector.trim), value = Strings.unquote(value.trim), column = Strings.unquote(column.trim))
}
