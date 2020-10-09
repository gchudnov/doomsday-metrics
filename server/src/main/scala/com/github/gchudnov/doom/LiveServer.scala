package com.github.gchudnov.doom

import com.github.gchudnov.doom.backends.http.HttpBackend
import com.github.gchudnov.doom.backends.shell.{ ShellBackend, ShellResult }
import com.github.gchudnov.doom.docs.{ CsvDoc, CsvOpts, JsonDoc, TextDoc }
import com.github.gchudnov.doom.probes._
import com.github.gchudnov.doom.queries.{ CsvQuery, JsonQuery, Query, TextQuery }
import com.github.gchudnov.doom.records.{ ErrorRecord, Record, ValueRecord }
import com.github.gchudnov.doom.values.Value
import zio._
import zio.clock._
import zio.stream._

final class LiveServer(
  clock: Clock.Service,
  http: HttpBackend.Service,
  shell: ShellBackend.Service,
  responseQueue: Queue[ProbeResponse],
  recordQueue: Queue[Record],
  conf: ServerConfig
) extends Server.Service {

  override def addProbe(p: Probe): UIO[ProbeId] =
    schedule(p)
      .provideLayer(ZLayer.succeed(clock))
      .fork
      .map(it => ProbeId(it))

  override def records: UIO[Stream[Nothing, Record]] = UIO.succeed(Stream.fromQueue(recordQueue))

  private def schedule(probe: Probe): ZIO[Clock, Nothing, Long] = {
    val result = probe match {
      case it: HttpProbe =>
        scheduleHttpProbe(it)
      case it: ShellProbe =>
        scheduleShellProbe(it)
    }

    result
      .retryN(conf.retryCount)
      .catchAll { t =>
        instant
          .map(at => ProbeErrorResponse(probe = probe, at = at, err = t))
      }
      .flatMap(probeResult => responseQueue.offer(probeResult))
      .repeat(
        Schedule.spaced(probe.interval)
      )
  }

  private def scheduleHttpProbe(probe: HttpProbe): ZIO[Clock, Throwable, ProbeResponse] =
    for {
      httpResult <- http.get(probe.url)
      at         <- instant
    } yield ProbeValueResponse(probe = probe, at = at, value = httpResult): ProbeResponse

  private def scheduleShellProbe(probe: ShellProbe): ZIO[Clock, Throwable, ProbeResponse] =
    for {
      shellResult <- shell.exec(probe.cmd)
      at          <- instant
    } yield shellResult.code match {
      case 0 => ProbeValueResponse(probe = probe, at = at, value = shellResult.out): ProbeResponse
      case _ => ProbeErrorResponse(probe = probe, at = at, err = ShellResult.toError(shellResult)): ProbeResponse
    }
}

object LiveServer {

  final case class DocState(json: Option[JsonDoc] = None, csv: Option[CsvDoc] = None, text: Option[TextDoc] = None)

  def responseLoop(recordQueue: Queue[Record])(responseQueue: Queue[ProbeResponse]): ZManaged[Any, Nothing, Fiber.Runtime[Throwable, Unit]] =
    ZStream
      .fromQueue(responseQueue)
      .mapM { response =>
        processProbeResponse(recordQueue)(response)
      }
      .runDrain
      .toManaged_
      .fork

  private[doom] def processProbeResponse(recordQueue: Queue[Record])(response: ProbeResponse): Task[Unit] =
    response match {
      case response: ProbeValueResponse =>
        processProbeValueResponse(recordQueue)(response)
      case er: ProbeErrorResponse =>
        processProbeErrorResponse(recordQueue)(er)
    }

  private def processProbeValueResponse(recordQueue: Queue[Record])(response: ProbeValueResponse): Task[Unit] =
    for {
      _ <- ZIO.foldLeft(response.probe.queries)(DocState()) { (state, query) =>
             for {
               (docState, record) <- processQuery(state)(query, response.value)
                                       .map({ case (updState, value) => (updState, ValueRecord(probe = response.probe, query = query, at = response.at, value = value)) })
                                       .catchAll(err => UIO((state, ErrorRecord(probe = response.probe, query = query, at = response.at, t = err): Record)))
               _ <- recordQueue.offer(record)
             } yield docState
           }
    } yield ()

  private def processProbeErrorResponse(recordQueue: Queue[Record])(er: ProbeErrorResponse): UIO[Unit] =
    for {
      _ <- ZIO.foreach(er.probe.queries) { query =>
             recordQueue.offer(ErrorRecord(probe = er.probe, query = query, at = er.at, t = er.err))
           }
    } yield ()

  private[doom] def processQuery(state: DocState)(query: Query, value: String): Task[(DocState, Value)] =
    query match {
      case q: CsvQuery =>
        for { (csvDoc, csvValue) <- processCsvQuery(state)(q, value) } yield (state.copy(csv = Some(csvDoc)), csvValue)
      case q: JsonQuery =>
        for { (jsonDoc, jsonValue) <- processJsonQuery(state)(q, value) } yield (state.copy(json = Some(jsonDoc)), jsonValue)
      case q: TextQuery =>
        for { (textDoc, textValue) <- processTextQuery(state)(q, value) } yield (state.copy(text = Some(textDoc)), textValue)
    }

  private[doom] def processCsvQuery(state: DocState)(query: CsvQuery, value: String): Task[(CsvDoc, Value)] =
    for {
      csvDoc   <- ZIO.fromOption(state.csv).catchAll(_ => CsvDoc.parse(value, CsvOpts(separator = query.separator)))
      csvValue <- Value.fromCsv(csvDoc, query.csvPath, query.valueType)
    } yield (csvDoc, csvValue)

  private[doom] def processJsonQuery(state: DocState)(query: JsonQuery, value: String): Task[(JsonDoc, Value)] =
    for {
      jsonDoc   <- ZIO.fromOption(state.json).catchAll(_ => JsonDoc.parse(value))
      jsonValue <- Value.fromJson(jsonDoc, query.jsonPath, query.valueType)
    } yield (jsonDoc, jsonValue)

  private[doom] def processTextQuery(state: DocState)(query: TextQuery, value: String): Task[(TextDoc, Value)] =
    for {
      txtDoc   <- ZIO.fromOption(state.text).catchAll(_ => TextDoc.parse(value))
      txtValue <- Value.fromText(txtDoc, query.textPath, query.valueType)
    } yield (txtDoc, txtValue)

}
