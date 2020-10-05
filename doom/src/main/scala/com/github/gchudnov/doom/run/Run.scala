package com.github.gchudnov.doom.run

import com.github.gchudnov.doom.records.Record
import com.github.gchudnov.swearwolf._
import zio._
import zio.clock.Clock
import zio.config.ZConfig

object Run {
  type Run = Has[Run.Service]

  trait Service {
    def shutdown(): UIO[Unit]
    def onRecord(rec: Record): UIO[Unit]
    def onKeySeq(ks: KeySeq): UIO[Unit]
    def processLoop(): RIO[Clock with Has[Screen], Unit]
  }

  val any: ZLayer[Run, Nothing, Run] =
    ZLayer.requires[Run]

  val live: ZLayer[ZConfig[RunConfig], Nothing, Run] = {
    val managed = for {
      conf        <- ZManaged.access[ZConfig[RunConfig]](_.get)
      recordQueue <- Queue.bounded[Record](conf.queueSize).toManaged(_.shutdown)
      keqSeqQueue <- Queue.bounded[KeySeq](conf.queueSize).toManaged(_.shutdown)
    } yield new LiveRun(recordQueue, keqSeqQueue, conf)
    ZLayer.fromManaged(managed)
  }

  def onRecord(rec: Record): RIO[Run, Unit]                     = ZIO.accessM(_.get.onRecord(rec))
  def onKeySeq(ks: KeySeq): RIO[Run, Unit]                      = ZIO.accessM(_.get.onKeySeq(ks))
  def processLoop(): RIO[Run with Clock with Has[Screen], Unit] = ZIO.accessM(_.get.processLoop())
  def shutdown(): URIO[Run, Unit]                               = ZIO.accessM(_.get.shutdown())
}
