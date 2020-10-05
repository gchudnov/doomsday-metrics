package com.github.gchudnov.doom

import com.github.gchudnov.doom.Server.{ addProbe, records, Server }
import com.github.gchudnov.doom.backends.http.HttpBackend
import com.github.gchudnov.doom.backends.shell.ShellBackend
import com.github.gchudnov.doom.probes.ShellProbe
import com.github.gchudnov.doom.queries.TextQuery
import com.github.gchudnov.doom.records.Record
import com.github.gchudnov.doom.values.DoubleValueType
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._
import zio.clock.Clock
import zio.console._
import zio.duration._
import zio.test.Assertion._
import zio.test._

object ServerITSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("ServerIT")(
      testM("random numbers can be generated") {
        val scriptPath = s"${System.getProperty("user.dir")}/examples/random.sh"
        val env        = makeEnv()

        val textQuery  = TextQuery(name = "text-query", textPath = "", valueType = DoubleValueType)
        val shellProbe = ShellProbe(name = "cpu-info", cmd = scriptPath, queries = Seq(textQuery), interval = 1.second)

        val program = for {
          probeId <- addProbe(shellProbe)
          recs    <- records
          _ <- recs
                 .take(5)
                 .foreach { rec =>
                   putStrLn(s"${rec.at}: ${Record.toString(rec)}")
                 }
        } yield ()

        program
          .provideSomeLayer[Clock with Console](env)
          .map(n => assert(n)(isUnit))
      }
    )

  private def makeEnv(): ZLayer[Clock, Throwable, Server] = {
    val clockEnv = Clock.live
    val confEnv  = ServerConfig.live
    val shellEnv = ShellBackend.live
    val sttpEnv  = AsyncHttpClientZioBackend.layer()
    val httpEnv  = sttpEnv >>> HttpBackend.live

    val env = (confEnv ++ clockEnv ++ httpEnv ++ shellEnv) >>> Server.live

    env
  }
}
