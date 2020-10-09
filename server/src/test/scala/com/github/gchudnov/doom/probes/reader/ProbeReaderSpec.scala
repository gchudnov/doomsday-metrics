package com.github.gchudnov.doom.probes.reader

import com.github.gchudnov.doom.probes.{ HttpProbe, ShellProbe }
import com.github.gchudnov.doom.queries.{ CsvQuery, JsonQuery, Query, TextQuery }
import com.github.gchudnov.doom.util.Resources
import com.github.gchudnov.doom.values.{ BigDecimalValueType, DoubleValueType, StringValueType }
import zio.ZIO
import zio.duration._
import zio.test.Assertion._
import zio.test._

object ProbeReaderSpec extends DefaultRunnableSpec {
  import ProbeReader._

  override def spec: ZSpec[Environment, Failure] =
    suite("ProbeReader")(
      testM("probes can be loaded from a file") {
        val env = ProbeReader.live

        val program = for {
          data   <- ZIO.fromEither(Resources.string("desc/probes.json"))
          actual <- fromString(data)
        } yield {
          val expected = List(
            ShellProbe(
              name = "test-shell-probe",
              cmd = "echo \"123\"",
              queries = Seq(
                CsvQuery(name = "csv-query", csvPath = "name=as.node.ignore.left.demand;name", separator = ";", valueType = StringValueType),
                JsonQuery(name = "json-query", jsonPath = "$.gauges.['threadStates.deadlock.count'].value", valueType = DoubleValueType),
                TextQuery(name = "text-query", textPath = "123", valueType = BigDecimalValueType)
              ),
              interval = 10.second
            ),
            HttpProbe(name = "test-http-probe", url = "http://localhost:7979/metrics", queries = Seq.empty[Query], interval = 30.second)
          )
          assert(actual)(equalTo(expected))
        }

        program.provideLayer(env)
      }
    )
}
