package com.github.gchudnov.doom.docs

import java.time.OffsetDateTime

import zio._

final class TxtDocException(message: String, t: Throwable) extends RuntimeException(message, t) {
  def this(message: String) = {
    this(message, null)
  }
}

final case class TextDoc(str: String) extends Doc {
  import TextDoc._

  def string(textPath: String): Task[String] =
    value(textPath)

  def double(textPath: String): Task[Double] =
    string(textPath).flatMap(it => Task(it.toDouble).mapError(t => new TxtDocException(s"cannot convert '${it}' value to double", t)))

  def long(textPath: String): Task[Long] =
    string(textPath).flatMap(it => Task(it.toLong).mapError(t => new TxtDocException(s"cannot convert '${it}' value to long", t)))

  def boolean(textPath: String): Task[Boolean] =
    string(textPath).flatMap(it => Task(it.toBoolean).mapError(t => new TxtDocException(s"cannot convert '${it}' value to boolean", t)))

  def bigDecimal(textPath: String): Task[BigDecimal] =
    string(textPath).flatMap(it => Task(BigDecimal(it)).mapError(t => new TxtDocException(s"cannot convert '${it}' value to big-decimal", t)))

  def dateTime(textPath: String): Task[OffsetDateTime] =
    string(textPath).flatMap(it => Task(OffsetDateTime.parse(it)).mapError(t => new TxtDocException(s"cannot convert '${it}' value to offset-date-time", t)))

  def value(textPath: String): Task[String] = {
    val rx = if (textPath.nonEmpty) s"${textPath}".r else SelectAll
    ZIO.fromOption(rx.findFirstIn(str)).mapError(_ => new TxtDocException(s"cannot find the value specified by regex: '${rx.toString()}'"))
  }
}

object TextDoc {
  private val SelectAll = "[\\S\\s]*".r

  def parse(value: String): Task[TextDoc] =
    Task(TextDoc(value))
}
