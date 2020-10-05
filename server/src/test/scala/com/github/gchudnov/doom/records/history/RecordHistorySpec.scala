package com.github.gchudnov.doom.records.history

import zio.test.{ suite, testM, DefaultRunnableSpec, ZSpec }

object RecordHistorySpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("RecordHistory")(
      testM("can be accumulated") {
        ???
      }
    )
}
