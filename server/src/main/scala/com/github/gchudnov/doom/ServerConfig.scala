package com.github.gchudnov.doom

import com.typesafe.config.ConfigFactory
import zio._
import zio.config._
import zio.config.magnolia.DeriveConfigDescriptor
import zio.config.typesafe._

final case class ServerConfig(
  queueSize: Int,
  retryCount: Int
)

object ServerConfig {
  private val configDesc: ConfigDescriptor[ServerConfig] = DeriveConfigDescriptor.descriptor[ServerConfig].mapKey(toKebabCase)

  val live: Layer[Throwable, ZConfig[ServerConfig]] = TypesafeConfig.fromTypesafeConfig(ConfigFactory.load.resolve.getConfig("server"), configDesc)
}
