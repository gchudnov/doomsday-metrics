package com.github.gchudnov.doom.history

import com.github.gchudnov.doom.probes.ProbeResponse
import zio._

/**
 * A service to store probe responses.
 */
object History {
  type History = Has[Service]

  trait Service {
    def store(pr: ProbeResponse): Task[Unit]
  }

  val file: ZLayer[Any, Nothing, History] = ZLayer.succeed(new FileHistory())
}
