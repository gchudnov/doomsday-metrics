package com.github.gchudnov.doom.ui.table

import zio.test.Assertion._
import zio.test._

object TableDashboardSpec extends DefaultRunnableSpec {

  override def spec: Spec[Environment, TestFailure[Throwable], TestSuccess] =
    suite("TableDashboard")(
      test("pager can be created") {

        val richText = TableDashboard.makePager(5, 1).toTry.get

        assert(richText.bytes.length)(isGreaterThan(0))
      }
    )

}
