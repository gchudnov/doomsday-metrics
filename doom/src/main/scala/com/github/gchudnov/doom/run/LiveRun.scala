package com.github.gchudnov.doom.run

import com.github.gchudnov.doom.records.Record
import com.github.gchudnov.doom.ui.Dashboard
import com.github.gchudnov.swearwolf._
import zio.{ RIO, _ }
import zio.clock.Clock
import zio.stream.ZStream

/**
 * Runner that renders updates on the given Screen.
 */
final class LiveRun(recordQueue: Queue[Record], keqSeqQueue: Queue[KeySeq], conf: RunConfig) extends Run.Service {

  val recordStream: ZStream[Clock, Nothing, Chunk[Record]] =
    ZStream
      .fromQueue(recordQueue)
      .groupedWithin(conf.chunkSize, conf.duration)

  val keySeqStream: ZStream[Any, Nothing, KeySeq] =
    ZStream.fromQueue(keqSeqQueue)

  override def shutdown(): UIO[Unit] =
    for {
      _ <- recordQueue.shutdown
      _ <- keqSeqQueue.shutdown
    } yield ()

  override def onRecord(u: Record): UIO[Unit] =
    recordQueue.offer(u).map(_ => ())

  override def onKeySeq(ks: KeySeq): UIO[Unit] =
    keqSeqQueue.offer(ks).map(_ => ())

  override def processLoop(): RIO[Clock with Has[Screen], Unit] =
    recordStream
      .mergeWith(keySeqStream)(recChunk => Left(recChunk), ks => Right(ks))
      .mapAccum(Dashboard.empty) { (d, it) =>
        it match {
          case Left(chunk) =>
            val updDashboard = d.update(chunk.toList)
            (updDashboard, Some(updDashboard): Option[Dashboard])
          case Right(ks) =>
            val updDashboard = d.keyseq(ks)
            val od = if ((d != updDashboard) || (ks.isSize)) Some(updDashboard) else None
            (updDashboard, od)
        }
      }
      .mapM(_.fold[RIO[Has[Screen], Unit]](ZIO.succeed(()))(render))
      .runDrain

  private def render(dashboard: Dashboard): RIO[Has[Screen], Unit] =
    for {
      screen <- ZIO.access[Has[Screen]](_.get)
      _ <- ZIO.fromEither(
             for {
               _ <- screen.clear()
               _ <- dashboard.render(screen)
               _ <- screen.flush()
             } yield ()
           )
    } yield ()

}
