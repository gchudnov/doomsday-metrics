package com.github.gchudnov.doom

import java.time.{ Instant, OffsetDateTime }

import com.github.gchudnov.doom.backends.http.HttpBackend
import com.github.gchudnov.doom.backends.http.HttpBackend.HttpBackend
import com.github.gchudnov.doom.backends.shell.ShellBackend.ShellBackend
import com.github.gchudnov.doom.backends.shell.{ ShellBackend, ShellResult }
import com.github.gchudnov.doom.docs.{ CsvDocException, CsvOpts }
import com.github.gchudnov.doom.probes._
import com.github.gchudnov.doom.queries.{ CsvQuery, JsonQuery, TextQuery }
import com.github.gchudnov.doom.records.{ ErrorRecord, Record, ValueRecord }
import com.github.gchudnov.doom.util.Resources
import com.github.gchudnov.doom.values.{ LongValue, LongValueType, StringValue, StringValueType }
import zio._
import zio.clock._
import zio.duration._
import zio.test.Assertion._
import zio.test.AssertionM.Render.param
import zio.test._
import zio.test.environment.TestClock._
import zio.test.environment.{ Live, TestClock, TestEnvironment }
import zio.test.mock.Expectation._
import zio.test.mock.Mock

object ServerSpec extends DefaultRunnableSpec {
  import LiveServer._
  import RecordEquality._
  import Server._

  private val csvOpts                         = CsvOpts(separator = ";")
  private val csvStrM: IO[Throwable, String]  = ZIO.fromEither(Resources.string("data/metrics.csv"))
  private val jsonStrM: IO[Throwable, String] = ZIO.fromEither(Resources.string("data/metrics.json"))
  private val textM: IO[Throwable, String]    = ZIO.fromEither(Resources.string("data/metrics.txt"))

  private val notUsedText  = "NOT_USED_IN_THIS_TEST"
  private val responseText = "123"

  private val textQuery  = TextQuery(name = "text-query", textPath = "", valueType = StringValueType)
  private val httpUrl    = "http://some-addr.org/metrics"
  private val httpProbe  = HttpProbe(name = "http-test-probe", url = httpUrl, queries = Seq(textQuery), interval = 1.second)
  private val shellProbe = ShellProbe(name = "shell-test-probe", cmd = s"echo ${responseText}", queries = Seq(textQuery), interval = 1.second)

  private val badCsvQuery = CsvQuery(name = "csv-query", csvPath = "BAD_PATH", separator = ",", valueType = StringValueType)
  private val badCsvProbe = ShellProbe(name = "shell-test-csv-probe", cmd = "cat /some/path", queries = Seq(badCsvQuery), interval = 1.second)

  private val t0 = OffsetDateTime.parse("2030-01-01T00:00:00Z")
  private val i0 = Instant.from(t0)

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite("Server")(
      testM("CsvQuery can be processed if the initial state is empty") {
        val state: DocState = DocState()
        val csvQuery: CsvQuery =
          CsvQuery(name = "some-query", csvPath = "name=as.node.ignore.left.downstream-finish;count", separator = csvOpts.separator, valueType = LongValueType)

        for {
          str             <- csvStrM
          (csvDoc, value) <- processCsvQuery(state)(csvQuery, str)
        } yield {
          val expected = LongValue(0L)
          assert(csvDoc.m)(isNonEmpty) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("CsvQuery can be processed if the initial state is non-empty") {
        val state: DocState = DocState()
        val csvQuery: CsvQuery =
          CsvQuery(name = "some-query", csvPath = "name=as.node.ignore.left.downstream-finish;count", separator = csvOpts.separator, valueType = LongValueType)

        for {
          str             <- csvStrM
          (csvDoc0, _)    <- processCsvQuery(state)(csvQuery, str)
          (csvDoc, value) <- processCsvQuery(state.copy(csv = Some(csvDoc0)))(csvQuery, "NOT_IMPORTANT_NOW;PARSING_MUST_BE_SKIPPED")
        } yield {
          val expected = LongValue(0L)
          assert(csvDoc.m)(isNonEmpty) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("JsonQuery can be processed if the initial state is empty") {
        val state: DocState      = DocState()
        val jsonQuery: JsonQuery = JsonQuery(name = "some-query", jsonPath = "$.histograms.['source.event.delay'].count", valueType = LongValueType)

        for {
          str              <- jsonStrM
          (jsonDoc, value) <- processJsonQuery(state)(jsonQuery, str)
        } yield {
          val expected = LongValue(4L)
          assert(jsonDoc.document.jsonString())(isNonEmptyString) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("JsonQuery can be processed if the initial state is non-empty") {
        val state: DocState      = DocState()
        val jsonQuery: JsonQuery = JsonQuery(name = "some-query", jsonPath = "$.histograms.['source.event.delay'].count", valueType = LongValueType)

        for {
          str              <- jsonStrM
          (jsonDoc0, _)    <- processJsonQuery(state)(jsonQuery, str)
          (jsonDoc, value) <- processJsonQuery(state.copy(json = Some(jsonDoc0)))(jsonQuery, "NOT_IMPORTANT_NOW;PARSING_MUST_BE_SKIPPED")
        } yield {
          val expected = LongValue(4L)
          assert(jsonDoc.document.jsonString())(isNonEmptyString) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("TextQuery can be processed if the initial state is empty") {
        val state: DocState      = DocState()
        val textQuery: TextQuery = TextQuery(name = "some-query", textPath = """Some\(\d+\)""", valueType = StringValueType)

        for {
          str              <- textM
          (textDoc, value) <- processTextQuery(state)(textQuery, str)
        } yield {
          val expected = StringValue("Some(345)")
          assert(textDoc.str)(isNonEmptyString) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("TextQuery can be processed if the initial state is non-empty") {
        val state: DocState      = DocState()
        val textQuery: TextQuery = TextQuery(name = "some-query", textPath = """Some\(\d+\)""", valueType = StringValueType)

        for {
          str              <- textM
          (textDoc0, _)    <- processTextQuery(state)(textQuery, str)
          (textDoc, value) <- processTextQuery(state.copy(text = Some(textDoc0)))(textQuery, "NOT_IMPORTANT_NOW;PARSING_MUST_BE_SKIPPED")
        } yield {
          val expected = StringValue("Some(345)")
          assert(textDoc.str)(isNonEmptyString) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("CsvQuery can be processed and an updated state is returned") {
        val state0: DocState = DocState()
        val csvQuery: CsvQuery =
          CsvQuery(name = "some-query", csvPath = "name=as.node.ignore.left.downstream-finish;count", separator = csvOpts.separator, valueType = LongValueType)

        for {
          str             <- csvStrM
          (state1, value) <- processQuery(state0)(csvQuery, str)
        } yield {
          val expected = LongValue(0L)
          assert(state1)(equalTo(state0).negate) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("JsonQuery can be processed and an updated state is returned") {
        val state0: DocState     = DocState()
        val jsonQuery: JsonQuery = JsonQuery(name = "some-query", jsonPath = "$.histograms.['source.event.delay'].count", valueType = LongValueType)

        for {
          str             <- jsonStrM
          (state1, value) <- processQuery(state0)(jsonQuery, str)
        } yield {
          val expected = LongValue(4L)
          assert(state1)(equalTo(state0).negate) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("TextQuery can be processed and an updated state is returned") {
        val state0: DocState     = DocState()
        val textQuery: TextQuery = TextQuery(name = "some-query", textPath = """Some\(\d+\)""", valueType = StringValueType)

        for {
          str             <- textM
          (state1, value) <- processQuery(state0)(textQuery, str)
        } yield {
          val expected = StringValue("Some(345)")
          assert(state1)(equalTo(state0).negate) &&
          assert(value)(equalTo(expected))
        }
      },
      testM("error probe response can be processed") {
        val t  = new RuntimeException("some problem")
        val er = ProbeErrorResponse(probe = shellProbe, at = i0, err = t)

        for {
          recordQueue <- Queue.unbounded[Record]
          _           <- processProbeResponse(recordQueue)(er)
          results     <- ZIO.collectAll(List.fill(1)(recordQueue.take))
        } yield {
          val expected = List(ErrorRecord(probe = shellProbe, query = textQuery, at = i0, t = t))
          assert(results)(equalTo(expected))
        }
      },
      testM("value probe response can be processed") {
        val r = ProbeValueResponse(probe = shellProbe, at = i0, value = "123")

        for {
          _           <- setDateTime(t0)
          recordQueue <- Queue.unbounded[Record]
          _           <- processProbeResponse(recordQueue)(r)
          results     <- ZIO.collectAll(List.fill(1)(recordQueue.take))
        } yield {
          val expected = List(ValueRecord(probe = shellProbe, query = textQuery, at = i0, value = StringValue("123")))
          assert(results)(equalTo(expected))
        }
      },
      testM("value probe response can be processed if there is an error in processing") {
        val t = new CsvDocException("csv-path 'BAD_PATH' cannot be parsed.")
        val r = ProbeValueResponse(probe = badCsvProbe, at = i0, value = "a,b,c\n1,2,3")

        for {
          _           <- setDateTime(t0)
          recordQueue <- Queue.unbounded[Record]
          _           <- processProbeResponse(recordQueue)(r)
          results     <- ZIO.collectAll(List.fill(1)(recordQueue.take))
        } yield {
          val expected = List(ErrorRecord(probe = badCsvProbe, query = badCsvQuery, at = i0, t = t))
          assert(results)(equalTo(expected))
        }
      },
      testM("shell probe can be added") {
        val clockEnv                                  = Clock.any
        val confEnv                                   = ServerConfig.live
        val shellEnv: ULayer[ShellBackend]            = ShellBackendMock.Exec(isNonEmptyString, value(ShellResult(0, notUsedText, responseText))).atLeast(1)
        val httpEnv: ULayer[HttpBackend]              = HttpBackendMock.Get(equalTo(httpUrl), value(notUsedText)).atMost(0)
        val mockEnv: ZLayer[Clock, Throwable, Server] = (confEnv ++ clockEnv ++ httpEnv ++ shellEnv) >>> Server.live

        val program: ZIO[Server with TestClock with Live, Throwable, TestResult] = for {
          _      <- withFastClock.fork
          _      <- addProbe(shellProbe)
          rq     <- Queue.unbounded[Record]
          rs     <- records
          _      <- rs.foreach(i => rq.offer(i)).fork
          actual <- ZIO.collectAll(List.fill(1)(rq.take))
        } yield {
          val expected = List(ValueRecord(probe = shellProbe, query = textQuery, at = i0, value = StringValue(responseText)))
          assert(actual)(recEqualTo(expected))
        }

        program.provideSomeLayer[Clock with TestClock with Live](mockEnv)
      },
      testM("http probe can be added") {
        val clockEnv                                  = Clock.any
        val confEnv                                   = ServerConfig.live
        val shellEnv: ULayer[ShellBackend]            = ShellBackendMock.Exec(isNonEmptyString, value(ShellResult(0, notUsedText, notUsedText))).atMost(0)
        val httpEnv: ULayer[HttpBackend]              = HttpBackendMock.Get(equalTo(httpUrl), value(responseText)).atLeast(1)
        val mockEnv: ZLayer[Clock, Throwable, Server] = (confEnv ++ clockEnv ++ httpEnv ++ shellEnv) >>> Server.live

        val program: ZIO[Server with TestClock with Live, Throwable, TestResult] = for {
          _      <- withFastClock.fork
          _      <- addProbe(httpProbe)
          rq     <- Queue.unbounded[Record]
          rs     <- records
          _      <- rs.foreach(i => rq.offer(i)).fork
          actual <- ZIO.collectAll(List.fill(1)(rq.take))
        } yield {
          val expected = List(ValueRecord(probe = httpProbe, query = textQuery, at = i0, value = StringValue(responseText)))
          assert(actual)(recEqualTo(expected))
        }

        program.provideSomeLayer[Clock with TestClock with Live](mockEnv)
      },
      testM("probe can be cancelled") {
        val clockEnv                                  = Clock.any
        val confEnv                                   = ServerConfig.live
        val shellEnv: ULayer[ShellBackend]            = ShellBackendMock.Exec(isNonEmptyString, value(ShellResult(0, notUsedText, notUsedText))).atMost(0)
        val httpEnv: ULayer[HttpBackend]              = HttpBackendMock.Get(equalTo(httpUrl), value(responseText)).atLeast(1)
        val mockEnv: ZLayer[Clock, Throwable, Server] = (confEnv ++ clockEnv ++ httpEnv ++ shellEnv) >>> Server.live

        val program = for {
          _             <- withFastClock.fork
          probeId       <- addProbe(httpProbe)
          rq            <- Queue.unbounded[Record]
          rs            <- records
          _             <- rs.foreach(i => rq.offer(i)).fork
          actualRecords <- ZIO.collectAll(List.fill(1)(rq.take))
          _             <- ZIO.sleep(5.second)
          _             <- probeId.cancel()
          _             <- ZIO.sleep(5.second)
          expectedSize  <- rq.size
          _             <- ZIO.sleep(5.second)
          actualSize    <- rq.size
        } yield {
          val expectedRecords = List(ValueRecord(probe = httpProbe, query = textQuery, at = i0, value = StringValue(responseText)))
          assert(actualRecords)(recEqualTo(expectedRecords)) &&
          assert(actualSize)(equalTo(expectedSize))
        }

        program.provideSomeLayer[Clock with TestClock with Live](mockEnv)
      },
      testM("http probe can be retried before returning an error") {
        val clockEnv                       = Clock.any
        val confEnv                        = ServerConfig.live
        val shellEnv: ULayer[ShellBackend] = ShellBackendMock.Exec(isNonEmptyString, value(ShellResult(0, notUsedText, notUsedText))).atMost(0)
        val httpEnv: ULayer[HttpBackend] =
          HttpBackendMock.Get(equalTo(httpUrl), failure(new RuntimeException("Oops.. Network timeout."))) ++ HttpBackendMock.Get(equalTo(httpUrl), value(responseText)).atLeast(1)
        val mockEnv: ZLayer[Clock, Throwable, Server] = (confEnv ++ clockEnv ++ httpEnv ++ shellEnv) >>> Server.live

        val program: ZIO[Server with TestClock with Live, Throwable, TestResult] = for {
          _      <- withFastClock.fork
          _      <- addProbe(httpProbe)
          rq     <- Queue.unbounded[Record]
          rs     <- records
          _      <- rs.foreach(i => rq.offer(i)).fork
          actual <- ZIO.collectAll(List.fill(1)(rq.take))
        } yield {
          val expected = List(ValueRecord(probe = httpProbe, query = textQuery, at = i0, value = StringValue(responseText)))
          assert(actual)(recEqualTo(expected))
        }

        program.provideSomeLayer[Clock with TestClock with Live](mockEnv)
      },
      testM("http probe returns an error on failures and when attemps exceed the retry count") {
        val err = new RuntimeException("Oops.. Network timeout.")

        val clockEnv                       = Clock.any
        val confEnv                        = ServerConfig.live
        val shellEnv: ULayer[ShellBackend] = ShellBackendMock.Exec(isNonEmptyString, value(ShellResult(0, notUsedText, notUsedText))).atMost(0)
        val httpEnv: ULayer[HttpBackend] =
          HttpBackendMock.Get(equalTo(httpUrl), failure(err)).atLeast(4)
        val mockEnv: ZLayer[Clock, Throwable, Server] = (confEnv ++ clockEnv ++ httpEnv ++ shellEnv) >>> Server.live

        val program: ZIO[Server with TestClock with Live, Throwable, TestResult] = for {
          _      <- withFastClock.fork
          _      <- addProbe(httpProbe)
          rq     <- Queue.unbounded[Record]
          rs     <- records
          _      <- rs.foreach(i => rq.offer(i)).fork
          actual <- ZIO.collectAll(List.fill(1)(rq.take))
        } yield {
          val expected = List(ErrorRecord(probe = httpProbe, query = textQuery, at = i0, t = err))
          assert(actual)(recEqualTo(expected))
        }

        program.provideSomeLayer[Clock with TestClock with Live](mockEnv)
      }
    )

  private val withFastClock: ZIO[TestClock with Live, Nothing, Long] =
    Live.withLive(TestClock.adjust(1.seconds))(_.repeat(Schedule.spaced(10.millis)))
}

object HttpBackendMock extends Mock[HttpBackend] {

  object Get extends Effect[String, Throwable, String]

  val compose: URLayer[Has[mock.Proxy], HttpBackend] = ZLayer.fromServiceM { proxy =>
    withRuntime.map { rts =>
      new HttpBackend.Service {
        override def get(url: String): Task[String] = proxy(Get, url)
      }
    }
  }
}

object ShellBackendMock extends Mock[ShellBackend] {

  object Exec extends Effect[String, Throwable, ShellResult]

  val compose: URLayer[Has[mock.Proxy], ShellBackend] = ZLayer.fromServiceM { proxy =>
    withRuntime.map { rts =>
      new ShellBackend.Service {
        override def exec(cmd: String): Task[ShellResult] = proxy(Exec, cmd)
      }
    }
  }
}

object RecordEquality {

  private def isRecEq(a: Record, b: Record): Boolean =
    (a.probe == b.probe) &&
      (a.query == b.query) &&
      (Record.toString(a) == Record.toString(b))

  final def recEqualTo(expected: Record): Assertion[Record] =
    Assertion.assertion("recEqualTo")(param(expected)) { actual =>
      isRecEq(actual, expected)
    }

  final def recEqualTo(expected: List[Record]): Assertion[List[Record]] =
    Assertion.assertion("recEqualTo")(param(expected)) { actual =>
      expected.size == actual.size &&
      actual.zip(expected).forall { case (a, e) => isRecEq(a, e) }
    }

}
