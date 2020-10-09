package com.github.gchudnov.doom.run

import com.typesafe.config.ConfigFactory
import zio._
import zio.config._
import zio.config.magnolia.DeriveConfigDescriptor
import zio.config.typesafe._
import zio.duration.Duration

final case class RunConfig(
  queueSize: Int,
  chunkSize: Int,
  duration: Duration
)

object RunConfig {
  private val configDesc: ConfigDescriptor[RunConfig] = DeriveConfigDescriptor.descriptor[RunConfig].mapKey(toKebabCase)

  val live: Layer[Throwable, ZConfig[RunConfig]] = TypesafeConfig.fromTypesafeConfig(ConfigFactory.load.resolve.getConfig("run"), configDesc)
}
