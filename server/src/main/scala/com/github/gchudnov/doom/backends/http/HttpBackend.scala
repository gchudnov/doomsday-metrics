package com.github.gchudnov.doom.backends.http

import sttp.client.asynchttpclient.zio.SttpClient
import zio._

/**
 * Service to make http requests
 */
object HttpBackend {
  type HttpBackend = Has[Service]

  trait Service {
    def get(url: String): Task[String]
  }

  val live: ZLayer[SttpClient, Nothing, HttpBackend] =
    ZLayer.fromFunction[SttpClient, HttpBackend.Service]((sttpClient: SttpClient) => new LiveHttpBackend(sttpClient))

  def get(url: String): RIO[HttpBackend, String] = ZIO.accessM(_.get.get(url))
}
