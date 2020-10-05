package com.github.gchudnov.doom.util

import zio.test.Assertion.equalTo
import zio.test._

object StringsSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("Strings")(
      test("quotes can be trimmed if found") {
        val input = """"value""""

        val actual   = Strings.unquote(input)
        val expected = "value"

        assert(actual)(equalTo(expected))
      },
      test("quotes cannot be trimmed if not found") {
        val input = "value"

        val actual   = Strings.unquote(input)
        val expected = "value"

        assert(actual)(equalTo(expected))
      },
      test("quotes cannot be trimmed if right quote is missing") {
        val input = "\"value"

        val actual   = Strings.unquote(input)
        val expected = "\"value"

        assert(actual)(equalTo(expected))
      },
      test("quotes cannot be trimmed if left quote is missing") {
        val input = "value\""

        val actual   = Strings.unquote(input)
        val expected = "value\""

        assert(actual)(equalTo(expected))
      }
    )
}
