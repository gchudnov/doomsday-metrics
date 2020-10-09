package com.github.gchudnov.doom.ui.table

import java.time.Instant

import com.github.gchudnov.doom.probes.HttpProbe
import com.github.gchudnov.doom.queries.TextQuery
import com.github.gchudnov.doom.records.ValueRecord
import com.github.gchudnov.doom.records.history.RecordHistory
import com.github.gchudnov.doom.values.{DoubleValue, DoubleValueType, LongValueType, StringValue, StringValueType}
import com.github.gchudnov.swearwolf.util.{Point, Rect, Size}
import zio.duration._
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}

object PageLayoutSpec extends DefaultRunnableSpec {

  override def spec: Spec[Environment, TestFailure[Throwable], TestSuccess] =
    suite("PageLayout")(
      test("can position rows in 2 tables") {
        val rcArea = Rect(Point(0, 0), Size(100, 12))
        val now = Instant.now()

        val textQuery1 = TextQuery(name = "text-query", textPath = "", valueType = StringValueType)
        val textQuery2 = TextQuery(name = "long-query", textPath = "", valueType = LongValueType)
        val probe      = HttpProbe(name = "test-http-probe", url = "http://localhost:7979/metrics", queries = Seq(textQuery1, textQuery2), interval = 30.second)

        val rec = ValueRecord(probe = probe, query = textQuery1, at = now, value = StringValue("ONE"))

        val values = Range(0, 20)
        val rows = values.map(n => new NumericRow(name = s"${n}", history = RecordHistory.bounded(4).add(rec))).toList

        val pages = PageLayout.layout(rcArea = rcArea, rows = rows, pageIndexStart = 0)

        assert(pages.size)(equalTo(2)) &&
        assert(pages.head.id)(equalTo(0)) &&
        assert(pages.last.id)(equalTo(1))
      },
      test("can estimate the width properly") {
        val rcArea = Rect(Point(0, 0), Size(200, 12))
        val now = Instant.now()

        val textQuery = TextQuery(name = "1-under_replicated_partitions_1", textPath = "", valueType = DoubleValueType)
        val probe      = HttpProbe(name = "def", url = "http://localhost:7979/metrics", queries = Seq(textQuery), interval = 30.second)

        val rec1 = ValueRecord(probe = probe, query = textQuery, at = now, value = DoubleValue(12392054.30))
        val rec2 = ValueRecord(probe = probe, query = textQuery, at = now, value = DoubleValue(92054.30))

        val rows = List.fill(8)(new NumericRow(name = rec1.id, history = RecordHistory.bounded(Row.HistoryLen).add(rec1))) ++
          List.fill(8)(new NumericRow(name = rec2.id, history = RecordHistory.bounded(Row.HistoryLen).add(rec1)))

        val pages = PageLayout.layout(rcArea = rcArea, rows = rows, pageIndexStart = 0)

        assert(pages.size)(equalTo(1))
      }
    )

}
