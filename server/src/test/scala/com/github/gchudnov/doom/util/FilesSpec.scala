package com.github.gchudnov.doom.util

import java.io.File

import zio.ZIO
import zio.test.Assertion._
import zio.test._

object FilesSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("Files")(
      testM("can read file as a string") {
        val tmpFile = File.createTempFile("res-", "")
        tmpFile.deleteOnExit()

        for {
          _        <- ZIO.fromEither(Resources.save("data/metrics.csv", tmpFile.toPath))
          expected <- ZIO.fromEither(Resources.string("data/metrics.csv"))
          actual   <- ZIO.fromEither(Files.string(tmpFile))
        } yield assert(actual)(equalTo(expected))
      }
    )
}
