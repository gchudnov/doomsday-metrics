package com.github.gchudnov.doom.backends.http

import sttp.client._
import sttp.client.asynchttpclient.zio._
import sttp.model.StatusCode
import zio._

final class LiveHttpBackend(sttpClient: SttpClient) extends HttpBackend.Service {

  override def get(url: String): Task[String] = {
    val request = basicRequest.get(uri"${url}")

    val program = for {
      response <- SttpClient.send(request)
      data <- (response.code, response.body) match {
                case (StatusCode.NotFound, _) => ZIO.fail(new RuntimeException(s"URL '$url' not found"))
                case (_, Right(data))         => ZIO.succeed(data)
                case (_, Left(e))             => ZIO.fail(new RuntimeException(s"Invalid server response: ${e}"))
              }

    } yield data

    program.provide(sttpClient)
  }
}
