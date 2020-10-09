package com.github.gchudnov.doom.util

import java.io.File

import zio.test.Assertion._
import zio.test._

object ResourcesSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("Resources")(
      test("resource should be non-empty") {
        val errOrData = Resources.string("data/metrics.csv")
        assert(errOrData)(isRight) &&
        assert(errOrData.toTry.get)(isNonEmptyString)
      },
      test("saved resource should be stored") {
        val tmpFile = File.createTempFile("res-", "")
        tmpFile.deleteOnExit()
        val initLen = tmpFile.length()

        val errOrRes = Resources.save("data/metrics.csv", tmpFile.toPath)

        assert(initLen)(equalTo(0L)) &&
        assert(errOrRes)(isRight) &&
        assert(tmpFile.length())(isGreaterThan(0L))
      }
    )
}
