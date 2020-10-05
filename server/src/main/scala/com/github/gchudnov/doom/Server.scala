package com.github.gchudnov.doom

import com.github.gchudnov.doom.backends.http.HttpBackend.HttpBackend
import com.github.gchudnov.doom.backends.shell.ShellBackend.ShellBackend
import com.github.gchudnov.doom.probes.{ Probe, ProbeId, ProbeResponse }
import com.github.gchudnov.doom.records.Record
import zio._
import zio.clock.Clock
import zio.config.ZConfig
import zio.stream._

object Server {
  type Server = Has[Service]

  trait Service {
    def addProbe(p: Probe): UIO[ProbeId]
    def records: UIO[Stream[Throwable, Record]]
  }

  val any: ZLayer[Server, Nothing, Server] =
    ZLayer.requires[Server]

  val live: ZLayer[ShellBackend with HttpBackend with Clock with ZConfig[ServerConfig], Nothing, Server] = {
    val managedProgram: ZManaged[ShellBackend with HttpBackend with Clock with ZConfig[ServerConfig], Nothing, LiveServer] = for {
      conf          <- ZManaged.access[ZConfig[ServerConfig]](_.get)
      clock         <- ZManaged.access[Clock](_.get)
      http          <- ZManaged.access[HttpBackend](_.get)
      shell         <- ZManaged.access[ShellBackend](_.get)
      responseQueue <- Queue.bounded[ProbeResponse](conf.queueSize).toManaged(_.shutdown)
      recordQueue   <- Queue.bounded[Record](conf.queueSize).toManaged(_.shutdown)
      _             <- LiveServer.responseLoop(recordQueue)(responseQueue)
    } yield new LiveServer(clock, http, shell, responseQueue, recordQueue, conf)

    ZLayer.fromManaged(managedProgram)
  }

  def addProbe(p: Probe): RIO[Server, ProbeId]        = ZIO.accessM(_.get.addProbe(p))
  def records: RIO[Server, Stream[Throwable, Record]] = ZIO.accessM(_.get.records)
}
