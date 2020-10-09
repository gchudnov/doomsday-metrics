package com.github.gchudnov.doom.run

import java.time.Instant

import com.github.gchudnov.doom.probes.HttpProbe
import com.github.gchudnov.doom.queries.TextQuery
import com.github.gchudnov.doom.records.ValueRecord
import com.github.gchudnov.doom.screens.ScreenFactory
import com.github.gchudnov.doom.screens.ScreenFactory.ScreenFactory
import com.github.gchudnov.doom.util.Resources
import com.github.gchudnov.doom.values.{LongValue, LongValueType, StringValue, StringValueType}
import com.github.gchudnov.swearwolf.util.Size
import zio.clock.Clock
import zio.duration._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.{Live, TestClock, TestEnvironment}
import zio.{Schedule, ZIO, ZLayer}

object RunSpec extends DefaultRunnableSpec {
  import Run._

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite("Run")(
      testM("records can be displayed") {
        val now = Instant.now()

        val textQuery1 = TextQuery(name = "text-query", textPath = "", valueType = StringValueType)
        val textQuery2 = TextQuery(name = "long-query", textPath = "", valueType = LongValueType)
        val probe      = HttpProbe(name = "test-http-probe", url = "http://localhost:7979/metrics", queries = Seq(textQuery1, textQuery2), interval = 30.second)
        val records = List(
          ValueRecord(probe = probe, query = textQuery1, at = now, value = StringValue("ONE")),
          ValueRecord(probe = probe, query = textQuery2, at = now, value = LongValue(12))
        )

        val program = for {
          _             <- withFastClock.fork
          screenFactory <- ZIO.access[ScreenFactory](_.get)
          actual <- screenFactory
                      .make()
                      .use({ screen =>
                        for {
                          _ <- processLoop().provideSomeLayer[Clock with Run](ZLayer.succeed(screen)).fork
                          _ <- ZIO.foreach(records)(record => onRecord(record))
                          _ <- ZIO.sleep(10.seconds)
                          _ <- shutdown()
                        } yield {
                          val actual = screen.toString
                          actual
                        }
                      })
        } yield {
          val expected = Resources.string("run/run-spec-output.txt").toTry.get
          assert(actual)(equalTo(expected))
        }

        program.provideSomeLayer[Clock with TestClock with Live](defaultEnv)
      }
    )

  private val screenSize = Size(80, 50)

  private val defaultEnv: ZLayer[Clock, Throwable, Run with ScreenFactory] = {
    val clockEnv   = Clock.any
    val runConfEnv = RunConfig.live

    val scrEnv = ScreenFactory.test(screenSize)
    val runEnv = (clockEnv ++ runConfEnv) >>> Run.live

    val env = (runEnv ++ scrEnv)

    env
  }

  private val withFastClock: ZIO[TestClock with Live, Nothing, Long] =
    Live.withLive(TestClock.adjust(1.seconds))(_.repeat(Schedule.spaced(10.millis)))
}
