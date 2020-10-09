package com.github.gchudnov.doom

import java.io.File

import com.github.gchudnov.doom.Server._
import com.github.gchudnov.doom.backends.http.HttpBackend
import com.github.gchudnov.doom.backends.shell.ShellBackend
import com.github.gchudnov.doom.probes.reader.ProbeReader
import com.github.gchudnov.doom.probes.reader.ProbeReader.ProbeReader
import com.github.gchudnov.doom.run.Run._
import com.github.gchudnov.doom.run.{Run, RunConfig, RunException}
import com.github.gchudnov.doom.screens.ScreenFactory
import com.github.gchudnov.doom.screens.ScreenFactory._
import com.github.gchudnov.swearwolf.util.EventLoop
import com.github.gchudnov.doom.{BuildInfo => AppBuildInfo}
import scopt.{OParser, OParserBuilder}
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._
import zio.clock.Clock
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object Doom extends App {
  val builder: OParserBuilder[DoomConfig] = OParser.builder[DoomConfig]

  val parser: OParser[Unit, DoomConfig] = {
    import builder._
    OParser.sequence(
      programName(AppBuildInfo.name),
      head(AppBuildInfo.name, AppBuildInfo.version),
      help("help").text("prints this usage text."),
      version("version"),
      opt[Unit]("debug")
        .action((_, c) => c.copy(isDebug = true))
        .text("debug mode."),
      arg[File]("<probe-file>")
        .required()
        .valueName("<probe-file>")
        .action((x, c) => c.copy(probeFile = x))
        .text("path to json probe configuration.")
    )
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val oconf = OParser.parse(parser, args, DoomConfig())

    val program = for {
      config <- ZIO.fromOption(oconf).mapError(_ => new RunException("Cannot parse the arguments."))
      probes <- ProbeReader.fromFile(config.probeFile.toPath)
      _      <- ZIO.foreach(probes)(probe => addProbe(probe))
      recs   <- records
      _      <- recs.foreach(rec => Run.onRecord(rec)).fork
      _ <- makeScreen().use({ screen =>
             for {
               _ <- Run.processLoop().provideSomeLayer[Clock with Run](ZLayer.succeed(screen)).fork
               _ <- ZIO
                      .iterate(EventLoop.Action.empty)(EventLoop.isContinue)({ _ =>
                        for {
                          ks <- ZIO.fromEither(screen.eventPoll())
                          _  <- ZIO.foreach(ks)(Run.onKeySeq)
                          a  <- ZIO.fromEither(EventLoop.defaultHandler(ks))
                        } yield a
                      })
                      .ensuring(Run.shutdown())

             } yield ()
           })
    } yield ()

    val env = makeEnv()

    program
      .flatMapError(it => Logging.error(it.toString))
      .provideLayer(env)
      .fold(_ => ExitCode.failure, _ => ExitCode.success)
  }

  private def makeEnv(): ZLayer[Clock, Throwable, Logging with Server with Run with ProbeReader with ScreenFactory with Clock] = {
    val clockEnv = Clock.any

    val srvConfEnv = ServerConfig.live
    val shellEnv   = ShellBackend.live

    val sttpEnv = AsyncHttpClientZioBackend.layer()
    val httpEnv = sttpEnv >>> HttpBackend.live

    val srvEnv = (srvConfEnv ++ clockEnv ++ httpEnv ++ shellEnv) >>> Server.live

    val prbEnv = ProbeReader.live

    val scrEnv     = ScreenFactory.live
    val runConfEnv = RunConfig.live

    val logEnv = Slf4jLogger.make(logFormat = (_, logEntry) => logEntry)

    val runEnv = (clockEnv ++ runConfEnv) >>> Run.live

    val env = (logEnv ++ runEnv ++ srvEnv ++ prbEnv ++ scrEnv ++ clockEnv)

    env
  }

}
