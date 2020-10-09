package com.github.gchudnov.doom.backends.shell

import zio.ZLayer
import zio.test.Assertion.equalTo
import zio.test._

object ShellServiceSpec extends DefaultRunnableSpec {
  import ShellBackend._

  override def spec: ZSpec[Environment, Failure] =
    suite("ShellService")(
      testM("shell command can be executed") {
        val program =
          for {
            r <- exec("echo 'Hello World!'")
          } yield assert(r.code)(equalTo(0)) &&
            assert(r.err)(equalTo("")) &&
            assert(r.out)(equalTo("Hello World!"))

        program.provideLayer(defaultEnv)
      },
      testM("shell command can be executed that returns several lines") {
        val program =
          for {
            r <- exec("echo 'Hello\nWorld!'")
          } yield assert(r.code)(equalTo(0)) &&
            assert(r.err)(equalTo("")) &&
            assert(r.out)(equalTo("Hello\nWorld!"))

        program.provideLayer(defaultEnv)
      }
    )

  private val defaultEnv: ZLayer[Any, Nothing, ShellBackend] = {
    val shellEnv = ShellBackend.live
    val env      = shellEnv

    env
  }
}
