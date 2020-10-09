package com.github.gchudnov.doom.values

import java.time.OffsetDateTime

import com.github.gchudnov.doom.docs.{ CsvDoc, CsvOpts, JsonDoc, TextDoc }
import com.github.gchudnov.doom.util.Resources
import zio._
import zio.test.Assertion._
import zio.test._

object ValueSpec extends DefaultRunnableSpec {

  private val csvStrM: IO[Throwable, String] = ZIO.fromEither(Resources.string("data/metrics.csv"))
  private val csvOpts                        = CsvOpts(separator = ";")

  private val jsonStrM: IO[Throwable, String] = ZIO.fromEither(Resources.string("data/metrics.json"))

  override def spec: ZSpec[Environment, Failure] =
    suite("Value")(
      testM("StringValue can be fetched from csv") {
        val path = "name=as.node.ignore.left.demand;name"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- Value.fromCsv(doc, path, StringValueType)
        } yield {
          val expected = StringValue("as.node.ignore.left.demand")
          assert(actual)(equalTo(expected))
        }
      },
      testM("DoubleValue can be fetched from csv") {
        val path = "name=as.node.ignore.left.demand;max"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- Value.fromCsv(doc, path, DoubleValueType)
        } yield {
          val expected = DoubleValue(0.345)
          assert(actual)(equalTo(expected))
        }
      },
      testM("LongValue can be fetched from csv") {
        val path = "name=as.node.ignore.left.downstream-finish;count"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- Value.fromCsv(doc, path, LongValueType)
        } yield {
          val expected = LongValue(0L)
          assert(actual)(equalTo(expected))
        }
      },
      testM("BooleanValue can be fetched from csv") {
        val path = "name=as.node.ignore.left.downstream-finish;bool_value"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- Value.fromCsv(doc, path, BooleanValueType)
        } yield {
          val expected = BooleanValue(false)
          assert(actual)(equalTo(expected))
        }
      },
      testM("BigDecimalValue can be fetched from csv") {
        val path = "name=as.node.ignore.left.downstream-finish;ts"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- Value.fromCsv(doc, path, BigDecimalValueType)
        } yield {
          val expected = BigDecimalValue(BigDecimal("1572730123847"))
          assert(actual)(equalTo(expected))
        }
      },
      testM("DateTimeValue can be fetched from a csv") {
        val path = "name=as.node.ignore.left.downstream-finish;dt"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- Value.fromCsv(doc, path, DateTimeValueType)
        } yield {
          val expected = DateTimeValue(OffsetDateTime.parse("2012-04-23T18:25:43.512Z"))
          assert(actual)(equalTo(expected))
        }
      },
      testM("StringValue can be fetched from json") {
        val path = "$.timers.['db.data.read-value-batch'].duration_units"
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- Value.fromJson(doc, path, StringValueType)
        } yield {
          val expected = StringValue("milliseconds")
          assert(actual)(equalTo(expected))
        }
      },
      testM("DoubleValue can be fetched from json") {
        val path = "$.gauges.['mem.total.max'].value"
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- Value.fromJson(doc, path, DoubleValueType)
        } yield {
          val expected = DoubleValue(389283839.0)
          assert(actual)(equalTo(expected))
        }
      },
      testM("LongValue can be fetched from json") {
        val path = "$.histograms.['source.event.delay'].count"
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- Value.fromJson(doc, path, LongValueType)
        } yield {
          val expected = LongValue(4L)
          assert(actual)(equalTo(expected))
        }
      },
      testM("BooleanValue can be fetched from json") {
        val path = "$.values.is_active"
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- Value.fromJson(doc, path, BooleanValueType)
        } yield {
          val expected = BooleanValue(true)
          assert(actual)(equalTo(expected))
        }
      },
      testM("BigDecimalValue can be fetched from json") {
        val path = "$.meters.['db.data.read-value'].mean_rate"
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- Value.fromJson(doc, path, BigDecimalValueType)
        } yield {
          val expected = BigDecimalValue(BigDecimal("0.000595580804863836"))
          assert(actual)(equalTo(expected))
        }
      },
      testM("DateTimeValue can be fetched from json") {
        val path = "$.values.some_date"
        for {
          str    <- jsonStrM
          doc    <- JsonDoc.parse(str)
          actual <- Value.fromJson(doc, path, DateTimeValueType)
        } yield {
          val expected = DateTimeValue(OffsetDateTime.parse("2012-04-23T18:25:43.511Z"))
          assert(actual)(equalTo(expected))
        }
      },
      testM("StringValue can be fetched from text") {
        val path = ""
        for {
          str    <- ZIO.succeed("milliseconds")
          doc    <- TextDoc.parse(str)
          actual <- Value.fromText(doc, path, StringValueType)
        } yield {
          val expected = StringValue("milliseconds")
          assert(actual)(equalTo(expected))
        }
      },
      testM("DoubleValue can be fetched from text") {
        val path = ""
        for {
          str    <- ZIO.succeed("12.34")
          doc    <- TextDoc.parse(str)
          actual <- Value.fromText(doc, path, DoubleValueType)
        } yield {
          val expected = DoubleValue(12.34)
          assert(actual)(equalTo(expected))
        }
      },
      testM("LongValue can be fetched from text") {
        val path = ""
        for {
          str    <- ZIO.succeed("1572730123847")
          doc    <- TextDoc.parse(str)
          actual <- Value.fromText(doc, path, LongValueType)
        } yield {
          val expected = LongValue(1572730123847L)
          assert(actual)(equalTo(expected))
        }
      },
      testM("BooleanValue can be fetched from text") {
        val path = ""
        for {
          str    <- ZIO.succeed("true")
          doc    <- TextDoc.parse(str)
          actual <- Value.fromText(doc, path, BooleanValueType)
        } yield {
          val expected = BooleanValue(true)
          assert(actual)(equalTo(expected))
        }
      },
      testM("BigDecimalValue can be fetched from text") {
        val path = ""
        for {
          str    <- ZIO.succeed("1.181788611742825E-4")
          doc    <- TextDoc.parse(str)
          actual <- Value.fromText(doc, path, BigDecimalValueType)
        } yield {
          val expected = BigDecimalValue(BigDecimal("0.0001181788611742825"))
          assert(actual)(equalTo(expected))
        }
      },
      testM("DateTimeValue can be fetched from text") {
        val path = ""
        for {
          str    <- ZIO.succeed("2012-04-23T18:25:43.511Z")
          doc    <- TextDoc.parse(str)
          actual <- Value.fromText(doc, path, DateTimeValueType)
        } yield {
          val expected = DateTimeValue(OffsetDateTime.parse("2012-04-23T18:25:43.511Z"))
          assert(actual)(equalTo(expected))
        }
      }
    )
}
