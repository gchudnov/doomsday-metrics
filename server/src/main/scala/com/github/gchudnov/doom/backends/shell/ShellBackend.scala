package com.github.gchudnov.doom.backends.shell

import zio._

/**
 * Service to execute shell commands
 */
object ShellBackend {
  type ShellBackend = Has[Service]

  trait Service {
    def exec(cmd: String): Task[ShellResult]
  }

  val live: ZLayer[Any, Nothing, ShellBackend] =
    ZLayer.succeed(new LiveShellBackend())

  def exec(cmd: String): RIO[ShellBackend, ShellResult] = ZIO.accessM(_.get.exec(cmd))
}
