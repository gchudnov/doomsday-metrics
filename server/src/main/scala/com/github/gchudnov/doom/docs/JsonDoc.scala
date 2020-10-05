package com.github.gchudnov.doom.docs

import java.time.OffsetDateTime

import com.jayway.jsonpath.JsonPath.using
import com.jayway.jsonpath.{ Configuration, DocumentContext }
import zio._

final class JsonDocException(message: String, t: Throwable) extends RuntimeException(message, t) {
  def this(message: String) = {
    this(message, null)
  }
}

/**
 * JSON Document loading and value extraction using jsonPath.
 *
 * in query parameters, 'path' is a json-path of the value to fetch
 */
final case class JsonDoc(document: DocumentContext) extends Doc {

  def string(path: String): Task[String] =
    value[String](path)

  def double(path: String): Task[Double] =
    value[Double](path)

  def long(path: String): Task[Long] =
    Task(document.read[java.lang.Long](path, classOf[java.lang.Long]))

  def boolean(path: String): Task[Boolean] =
    value[Boolean](path)

  def bigDecimal(path: String): Task[BigDecimal] =
    Task(document.read[java.math.BigDecimal](path, classOf[java.math.BigDecimal]))

  def dateTime(path: String): Task[OffsetDateTime] =
    Task(document.read[String](path)).map(OffsetDateTime.parse(_))

  def value[T](path: String): Task[T] =
    Task(document.read[T](path)).mapError(t => new JsonDocException(s"cannot query the document with json-path: '${path}'", t))
}

object JsonDoc {
  def parse(value: String): Task[JsonDoc] = {
    val conf = Configuration.defaultConfiguration()
    Task(JsonDoc(using(conf).parse(value)))
  }
}
