package com.github.gchudnov.doom.screens

import com.github.gchudnov.swearwolf.Screen
import com.github.gchudnov.swearwolf.term.ArrayScreen
import com.github.gchudnov.swearwolf.util.Size
import zio._

object ScreenFactory {
  type ScreenFactory = Has[ScreenFactory.Service]

  trait Service {
    def make(): TaskManaged[Screen]
  }

  val any: ZLayer[ScreenFactory, Nothing, ScreenFactory] =
    ZLayer.requires[ScreenFactory]

  val live: ULayer[ScreenFactory] =
    ZLayer.succeed(() => ZManaged.make(ZIO.fromEither(Screen.acquire()))(sc => ZIO.fromEither(sc.shutdown()).orDie))

  def test(sz: Size): ULayer[ScreenFactory] =
    ZLayer.succeed(() => ZManaged.make(ZIO(ArrayScreen(sz)))(sc => ZIO.fromEither(sc.shutdown()).orDie))

  def makeScreen(): RManaged[ScreenFactory, Screen] =
    ZManaged.accessManaged[ScreenFactory](_.get.make())

}
