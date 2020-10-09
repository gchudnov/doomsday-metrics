package com.github.gchudnov.doom.docs

import java.time.OffsetDateTime

import com.github.gchudnov.doom.util.Resources
import zio.test.Assertion._
import zio.test._
import zio._

object JsonDocSpec extends DefaultRunnableSpec {

  private val jsonStrM: IO[Throwable, String] = ZIO.fromEither(Resources.string("data/metrics.json"))

  override def spec: ZSpec[Environment, Failure] =
    suite("JsonDoc")(
      testM("double value can be queried") {
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- doc.double("$.gauges.['threadStates.deadlock.count'].value")
        } yield {
          val expected = 0.0
          assert(actual)(equalTo(expected))
        }
      },
      testM("double value in scientific notation can be queried") {
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- doc.double("$.gauges.['mem.total.max'].value")
        } yield {
          val expected = 389283839.0
          assert(actual)(equalTo(expected))
        }
      },
      testM("non-existent key can be queried") {
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- doc.double("$.some.invalid.path").either
        } yield assert(actual)(isLeft)
      },
      testM("string value can be queried") {
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- doc.string("$.timers.['db.data.read-value-batch'].duration_units")
        } yield {
          val expected = "milliseconds"
          assert(actual)(equalTo(expected))
        }
      },
      testM("long value can be queried") {
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- doc.long("$.histograms.['source.event.delay'].count")
        } yield {
          val expected = 4L
          assert(actual)(equalTo(expected))
        }
      },
      testM("boolean value can be queried") {
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- doc.boolean("$.values.is_active")
        } yield {
          val expected = true
          assert(actual)(equalTo(expected))
        }
      },
      testM("bigDecimal value can be queried") {
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- doc.bigDecimal("$.meters.['db.data.read-value'].mean_rate")
        } yield {
          val expected = BigDecimal("0.000595580804863836")
          assert(actual)(equalTo(expected))
        }
      },
      testM("dateTime value can be queried") {
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- doc.dateTime("$.values.some_date")
        } yield {
          val expected = OffsetDateTime.parse("2012-04-23T18:25:43.511Z")
          assert(actual)(equalTo(expected))
        }
      }
    )
}
