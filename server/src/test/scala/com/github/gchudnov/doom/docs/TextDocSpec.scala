package com.github.gchudnov.doom.docs

import java.time.OffsetDateTime

import com.github.gchudnov.doom.util.Resources
import zio._
import zio.test.Assertion._
import zio.test._

object TextDocSpec extends DefaultRunnableSpec {

  private val textM: IO[Throwable, String] = ZIO.fromEither(Resources.string("data/metrics.txt"))

  override def spec: ZSpec[Environment, Failure] =
    suite("TextDoc")(
      testM("value in the wrong format can be queried") {
        for {
          str    <- ZIO.succeed("milliseconds")
          doc    <- TextDoc.parse(str)
          actual <- doc.double("").either
        } yield assert(actual)(isLeft)
      },
      testM("string value can be queried") {
        for {
          str    <- ZIO.succeed("milliseconds")
          doc    <- TextDoc.parse(str)
          actual <- doc.string("")
        } yield {
          val expected = "milliseconds"
          assert(actual)(equalTo(expected))
        }
      },
      testM("double value can be queried") {
        for {
          str    <- ZIO.succeed("12.34")
          doc    <- TextDoc.parse(str)
          actual <- doc.double("")
        } yield {
          val expected = 12.34
          assert(actual)(equalTo(expected))
        }
      },
      testM("long value can be queried") {
        for {
          str    <- ZIO.succeed("1572730123847")
          doc    <- TextDoc.parse(str)
          actual <- doc.long("")
        } yield {
          val expected = 1572730123847L
          assert(actual)(equalTo(expected))
        }
      },
      testM("boolean value can be queried") {
        for {
          str    <- ZIO.succeed("true")
          doc    <- TextDoc.parse(str)
          actual <- doc.boolean("")
        } yield {
          val expected = true
          assert(actual)(equalTo(expected))
        }
      },
      testM("bigDecimal value can be queried") {
        for {
          str    <- ZIO.succeed("1.181788611742825E-4")
          doc    <- TextDoc.parse(str)
          actual <- doc.bigDecimal("")
        } yield {
          val expected = BigDecimal("0.0001181788611742825")
          assert(actual)(equalTo(expected))
        }
      },
      testM("dateTime value can be queried") {
        for {
          str    <- ZIO.succeed("2012-04-23T18:25:43.511Z")
          doc    <- TextDoc.parse(str)
          actual <- doc.dateTime("")
        } yield {
          val expected = OffsetDateTime.parse("2012-04-23T18:25:43.511Z")
          assert(actual)(equalTo(expected))
        }
      },
      testM("part of the text can be queried using regex") {
        for {
          str    <- ZIO.succeed("Some(345)")
          doc    <- TextDoc.parse(str)
          actual <- doc.string("""Some\(\d+\)""")
        } yield {
          val expected = "Some(345)"
          assert(actual)(equalTo(expected))
        }
      },
      testM("default regex can extract the whole document") {
        for {
          str    <- textM
          doc    <- TextDoc.parse(str)
          actual <- doc.string("")
        } yield {
          val expected = str
          assert(actual)(equalTo(expected))
        }
      }
    )
}
