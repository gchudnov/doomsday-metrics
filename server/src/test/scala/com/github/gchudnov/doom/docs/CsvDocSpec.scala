package com.github.gchudnov.doom.docs

import java.time.OffsetDateTime

import com.github.gchudnov.doom.util.Resources
import zio._
import zio.test.Assertion._
import zio.test._

object CsvDocSpec extends DefaultRunnableSpec {

  private val csvStrM: IO[Throwable, String] = ZIO.fromEither(Resources.string("data/metrics.csv"))
  private val csvMskM: IO[Throwable, String] = ZIO.fromEither(Resources.string("data/metrics-msk.csv"))

  private val csvOpts      = CsvOpts(separator = ";")
  private val csvCommaOpts = CsvOpts(separator = ",")

  override def spec: ZSpec[Environment, Failure] =
    suite("CsvDoc")(
      testM("cell csv-path with special characters can be parsed") {
        val path = "name=as.node.ignore.left.demand;name"
        for {
          actual <- CsvDoc.csvPath(path)
        } yield {
          val expected = CsvDoc.CsvPath(selector = "name", value = "as.node.ignore.left.demand", column = "name")
          assert(actual)(equalTo(expected))
        }
      },
      testM("cell csv-path with spaces can be parsed") {
        val path = " name = as.node.ignore.left.demand  ;name "
        for {
          actual <- CsvDoc.csvPath(path)
        } yield {
          val expected = CsvDoc.CsvPath(selector = "name", value = "as.node.ignore.left.demand", column = "name")
          assert(actual)(equalTo(expected))
        }
      },
      testM("cell csv-path with quotes and spaces can be parsed") {
        val path = """"name" = "as.node.ignore.left.demand"  ;"name" """
        for {
          actual <- CsvDoc.csvPath(path)
        } yield {
          val expected = CsvDoc.CsvPath(selector = "name", value = "as.node.ignore.left.demand", column = "name")
          assert(actual)(equalTo(expected))
        }
      },
      testM("string column can be queried") {
        val path = "name=as.node.ignore.left.demand;name"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.string(path)
        } yield {
          val expected = "as.node.ignore.left.demand"
          assert(actual)(equalTo(expected))
        }
      },
      testM("double column can be queried") {
        val path = "name=as.node.ignore.left.demand;max"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.double(path)
        } yield {
          val expected = 0.345
          assert(actual)(equalTo(expected))
        }
      },
      testM("long column can be queried") {
        val path = "name=as.node.ignore.left.downstream-finish;count"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.long(path)
        } yield {
          val expected = 0L
          assert(actual)(equalTo(expected))
        }
      },
      testM("boolean column can be queried") {
        val path = "name=as.node.ignore.left.downstream-finish;bool_value"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.boolean(path)
        } yield {
          val expected = false
          assert(actual)(equalTo(expected))
        }
      },
      testM("bigDecimal column can be queried") {
        val path = "name=as.node.ignore.left.downstream-finish;ts"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.bigDecimal(path)
        } yield {
          val expected = BigDecimal("1572730123847")
          assert(actual)(equalTo(expected))
        }
      },
      testM("dateTime column can be queried") {
        val path = "name=as.node.ignore.left.downstream-finish;dt"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.dateTime(path)
        } yield {
          val expected = OffsetDateTime.parse("2012-04-23T18:25:43.512Z")
          assert(actual)(equalTo(expected))
        }
      },
      testM("completely invalid path cannot be queried") {
        val path = "ivalid"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.dateTime(path).either
        } yield assert(actual)(isLeft)
      },
      testM("csv-path with non-existent selector cannot be queried") {
        val path = "badname=as.node.ignore.left.downstream-finish;dt"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.dateTime(path).either
        } yield assert(actual)(isLeft)
      },
      testM("csv-path with non-existent selector-value cannot be queried") {
        val path = "name=bad-name;dt"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.dateTime(path).either
        } yield assert(actual)(isLeft)
      },
      testM("csv-path with non-existend column cannot be queried") {
        val path = "name=as.node.ignore.left.downstream-finish;badcolumn"
        for {
          str    <- csvStrM
          doc    <- CsvDoc.parse(str, csvOpts)
          actual <- doc.dateTime(path).either
        } yield assert(actual)(isLeft)
      },
      testM("double column can be queried if char separator is a comma") {
        val path = "Id=cpu_user_1;Value"
        for {
          str    <- csvMskM
          doc    <- CsvDoc.parse(str, csvCommaOpts)
          actual <- doc.double(path)
        } yield {
          val expected = 38.16786073994206
          assert(actual)(equalTo(expected))
        }
      }
    )

}
