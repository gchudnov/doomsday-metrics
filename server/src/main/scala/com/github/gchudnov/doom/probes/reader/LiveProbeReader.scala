package com.github.gchudnov.doom.probes.reader

import java.nio.file.Path
import java.util.{ List => JList, Map => JMap }

import com.github.gchudnov.doom.probes.{ HttpProbe, Probe, ShellProbe }
import com.github.gchudnov.doom.queries.{ CsvQuery, JsonQuery, Query, TextQuery }
import com.github.gchudnov.doom.util.Files
import com.github.gchudnov.doom.values.ValueType
import com.jayway.jsonpath.JsonPath.using
import com.jayway.jsonpath.{ Configuration, DocumentContext }
import zio._
import zio.duration._

import scala.concurrent.duration.{ Duration => SDuration }
import scala.jdk.CollectionConverters._

/**
 * Reads probe description to a Probe collection.
 */
final class LiveProbeReader() extends ProbeReader.Service {
  import LiveProbeReader._

  override def fromFile(path: Path): Task[List[Probe]] =
    for {
      data   <- ZIO.fromEither(Files.string(path.toFile))
      probes <- fromString(data)
    } yield probes

  override def fromString(data: String): Task[List[Probe]] = {
    val conf = Configuration.defaultConfiguration()

    for {
      doc    <- Task(using(conf).parse(data))
      probes <- parseProbes(doc)
    } yield probes
  }
}

object LiveProbeReader {

  private val AttrType     = "type"
  private val AttrName     = "name"
  private val AttrCmd      = "cmd"
  private val AttrUrl      = "url"
  private val AttrInterval = "interval"
  private val AttrQueries  = "queries"

  private val AttrCsvPath   = "csvPath"
  private val AttrSeparator = "separator"
  private val AttrValueType = "valueType"
  private val AttrJsonPath  = "jsonPath"
  private val AttrTextPath  = "textPath"

  private val ValueHttp  = "http"
  private val ValueShell = "shell"

  private val ValueCsv  = "csv"
  private val ValueJson = "json"
  private val ValueText = "text"

  private def parseProbes(doc: DocumentContext): Task[List[Probe]] =
    for {
      ds     <- Task(doc.read("$.*", classOf[JList[JMap[String, Object]]]).asScala.toList)
      probes <- ZIO.foreach(ds)(obj => parseProbe(obj.asScala.toMap))
    } yield probes

  private def parseProbe(m: Map[String, Object]): Task[Probe] =
    for {
      probeType <- ZIO.fromOption(m.get(AttrType)).mapError(_ => new ProbeReaderAttributeException(AttrType, m))
      probe <- probeType match {
                 case ValueHttp =>
                   parseHttpProbe(m)
                 case ValueShell =>
                   parseShellProbe(m)
               }
    } yield probe

  private def parseHttpProbe(m: Map[String, Object]): Task[HttpProbe] =
    for {
      name     <- parseNameAttr(m)
      url      <- parseUrlAttr(m)
      queries  <- parseQueriesAttr(m)
      interval <- parseIntervalAttr(m)
    } yield HttpProbe(
      name = name,
      url = url,
      queries = queries,
      interval = interval
    )

  private def parseShellProbe(m: Map[String, Object]): Task[ShellProbe] =
    for {
      name     <- parseNameAttr(m)
      cmd      <- parseCmdAttr(m)
      queries  <- parseQueriesAttr(m)
      interval <- parseIntervalAttr(m)
    } yield ShellProbe(
      name = name,
      cmd = cmd,
      queries = queries,
      interval = interval
    )

  private def parseNameAttr(m: Map[String, Object]): Task[String] =
    ZIO.fromOption(m.get(AttrName)).mapError(_ => new ProbeReaderAttributeException(AttrName, m)).flatMap(parseStringValue)

  private def parseUrlAttr(m: Map[String, Object]): Task[String] =
    ZIO.fromOption(m.get(AttrUrl)).mapError(_ => new ProbeReaderAttributeException(AttrUrl, m)).flatMap(parseStringValue)

  private def parseCmdAttr(m: Map[String, Object]): Task[String] =
    ZIO.fromOption(m.get(AttrCmd)).mapError(_ => new ProbeReaderAttributeException(AttrCmd, m)).flatMap(parseStringValue)

  private def parseQueriesAttr(m: Map[String, Object]): Task[List[Query]] =
    ZIO.fromOption(m.get(AttrQueries)).mapError(_ => new ProbeReaderAttributeException(AttrCmd, m)).flatMap(parseQueryValues)

  private def parseIntervalAttr(m: Map[String, Object]): Task[Duration] =
    ZIO.fromOption(m.get(AttrInterval)).mapError(_ => new ProbeReaderAttributeException(AttrInterval, m)).flatMap(parseIntervalValue)

  private def parseCsvPathAttr(m: Map[String, Object]): Task[String] =
    ZIO.fromOption(m.get(AttrCsvPath)).mapError(_ => new ProbeReaderAttributeException(AttrCsvPath, m)).flatMap(parseStringValue)

  private def parseJsonPathAttr(m: Map[String, Object]): Task[String] =
    ZIO.fromOption(m.get(AttrJsonPath)).mapError(_ => new ProbeReaderAttributeException(AttrJsonPath, m)).flatMap(parseStringValue)

  private def parseTextPathAttr(m: Map[String, Object]): Task[String] =
    ZIO.fromOption(m.get(AttrTextPath)).mapError(_ => new ProbeReaderAttributeException(AttrTextPath, m)).flatMap(parseStringValue)

  private def parseSeparatorAttr(m: Map[String, Object]): Task[String] =
    ZIO.fromOption(m.get(AttrSeparator)).mapError(_ => new ProbeReaderAttributeException(AttrSeparator, m)).flatMap(parseStringValue)

  private def parseValueTypeAttr(m: Map[String, Object]): Task[ValueType] =
    ZIO.fromOption(m.get(AttrValueType)).mapError(_ => new ProbeReaderAttributeException(AttrValueType, m)).flatMap(parseValueTypeValue)

  private def parseQueryValues(value: Object): Task[List[Query]] =
    for {
      ds      <- Task(value.asInstanceOf[JList[JMap[String, Object]]].asScala.toList)
      queries <- ZIO.foreach(ds)(obj => parseQuery(obj.asScala.toMap))
    } yield queries

  private def parseStringValue(value: Object): Task[String] =
    Task(value.toString)

  private def parseIntervalValue(value: Object): Task[Duration] =
    Task(SDuration(value.toString)).map(Duration.fromScala)

  private def parseValueTypeValue(value: Object): Task[ValueType] =
    Task.fromEither(ValueType(value.toString))

  private def parseQuery(m: Map[String, Object]): Task[Query] =
    for {
      queryType <- ZIO.fromOption(m.get(AttrType)).mapError(_ => new ProbeReaderAttributeException(AttrType, m))
      query <- queryType match {
                 case ValueCsv =>
                   parseCsvQuery(m)
                 case ValueJson =>
                   parseJsonQuery(m)
                 case ValueText =>
                   parseTextQuery(m)
               }
    } yield query

  private def parseCsvQuery(m: Map[String, Object]): Task[CsvQuery] =
    for {
      name      <- parseNameAttr(m)
      csvPath   <- parseCsvPathAttr(m)
      separator <- parseSeparatorAttr(m)
      valueType <- parseValueTypeAttr(m)
    } yield CsvQuery(
      name = name,
      csvPath = csvPath,
      separator = separator,
      valueType = valueType
    )

  private def parseJsonQuery(m: Map[String, Object]): Task[JsonQuery] =
    for {
      name      <- parseNameAttr(m)
      jsonPath  <- parseJsonPathAttr(m)
      valueType <- parseValueTypeAttr(m)
    } yield JsonQuery(
      name = name,
      jsonPath = jsonPath,
      valueType = valueType
    )

  private def parseTextQuery(m: Map[String, Object]): Task[TextQuery] =
    for {
      name      <- parseNameAttr(m)
      textPath  <- parseTextPathAttr(m)
      valueType <- parseValueTypeAttr(m)
    } yield TextQuery(
      name = name,
      textPath = textPath,
      valueType = valueType
    )
}
