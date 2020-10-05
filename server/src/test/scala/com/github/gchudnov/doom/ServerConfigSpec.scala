package com.github.gchudnov.doom

import zio._
import zio.config.ZConfig
import zio.test.Assertion._
import zio.test._

object ServerConfigSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("Server")(
      testM("default config can be loaded") {
        val confEnv = ServerConfig.live
        val program =
          for {
            conf <- ZIO.access[ZConfig[ServerConfig]](_.get)
          } yield assert(conf.retryCount)(isGreaterThan(0)) &&
            assert(conf.queueSize)(isGreaterThan(0))

        program.provideLayer(confEnv)
      }
    )
}
