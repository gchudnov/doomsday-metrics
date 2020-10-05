package com.github.gchudnov.doom.backends.http

import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.TaskLayer
import zio.test.Assertion._
import zio.test._

object HttpServiceSpec extends DefaultRunnableSpec {
  import HttpBackend._

  override def spec: ZSpec[Environment, Failure] =
    suite("HttpService")(
      testM("a get-request can be sent") {
        val url = "https://httpbin.org/get"

        val program = for {
          actual <- get(url)
        } yield {
          val expected = false
          assert(actual.isEmpty)(equalTo(expected))
        }

        program.provideLayer(defaultEnv)
      }
    )

  private val defaultEnv: TaskLayer[HttpBackend] = {
    val sttpEnv = AsyncHttpClientZioBackend.layer()
    val httpEnv = sttpEnv >>> HttpBackend.live
    val env     = httpEnv

    env
  }
}
