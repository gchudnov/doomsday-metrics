package com.github.gchudnov.doom.probes.reader

import java.nio.file.Path

import com.github.gchudnov.doom.probes.Probe
import zio._

object ProbeReader {
  type ProbeReader = Has[ProbeReader.Service]

  trait Service {
    def fromFile(path: Path): Task[List[Probe]]
    def fromString(data: String): Task[List[Probe]]
  }

  val live: ULayer[ProbeReader] = ZLayer.succeed(new LiveProbeReader())

  def fromFile(path: Path): RIO[ProbeReader, List[Probe]]     = ZIO.accessM(_.get.fromFile(path))
  def fromString(data: String): RIO[ProbeReader, List[Probe]] = ZIO.accessM(_.get.fromString(data))
}
