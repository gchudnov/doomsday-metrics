import sbt._

object Dependencies {
  object versions {
    val jsonpath            = "2.4.0"
    val kindProjector       = "0.10.3"
    val logbackClassic      = "1.2.3"
    val scalaLogging        = "3.9.2"
    val scopt               = "4.0.0-RC2"
    val zio                 = "1.0.0"
    val zioAsyncHttpBackend = "2.2.9"
    val zioConfig           = "1.0.0-RC27"
    val zioLogging          = "0.5.2"
    val fastparse           = "2.3.0"
    val swearwolf           = "1.0.0"
    val nativeImageSvm      = "20.2.0"
  }

  private val kindProjector = compilerPlugin(
    "org.typelevel" %% "kind-projector" % versions.kindProjector
  )

  private val compiler = Seq(
    kindProjector
  )

  private val nativeImageSvm = "org.graalvm.nativeimage" % "svm" % versions.nativeImageSvm

  private val internal = Seq(
    nativeImageSvm
  ).map(_ % "compile")

  private val logbackClassic = "ch.qos.logback" % "logback-classic" % versions.logbackClassic

  private val scopt    = "com.github.scopt"   %% "scopt"     % versions.scopt
  private val jsonpath = "com.jayway.jsonpath" % "json-path" % versions.jsonpath

  private val zio               = "dev.zio" %% "zio"                 % versions.zio
  private val zioConfig         = "dev.zio" %% "zio-config"          % versions.zioConfig
  private val zioConfigMagnolia = "dev.zio" %% "zio-config-magnolia" % versions.zioConfig
  private val zioConfigTypesafe = "dev.zio" %% "zio-config-typesafe" % versions.zioConfig
  private val zioLogging        = "dev.zio" %% "zio-logging"         % versions.zioLogging
  private val zioLoggingSlf4j   = "dev.zio" %% "zio-logging-slf4j"   % versions.zioLogging
  private val zioStreams        = "dev.zio" %% "zio-streams"         % versions.zio
  private val zioTest           = "dev.zio" %% "zio-test"            % versions.zio
  private val zioTestMagnolia   = "dev.zio" %% "zio-test-magnolia"   % versions.zio
  private val zioTestSbt        = "dev.zio" %% "zio-test-sbt"        % versions.zio

  private val zioAsyncHttpBackend = "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % versions.zioAsyncHttpBackend

  private val swearwolfCore  = "com.github.gchudnov" %% "swearwolf-core"  % versions.swearwolf
  private val swearwolfWoods = "com.github.gchudnov" %% "swearwolf-woods" % versions.swearwolf

  val Doom: Seq[ModuleID] = {
    val compile = Seq(
      jsonpath,
      logbackClassic,
      scopt,
      zio,
      zioAsyncHttpBackend,
      zioConfig,
      zioConfigMagnolia,
      zioConfigTypesafe,
      zioLogging,
      zioLoggingSlf4j,
      zioStreams,
      swearwolfCore,
      swearwolfWoods
    )
    val test = Seq(
      zioTest,
      zioTestMagnolia,
      zioTestSbt
    ) map (_ % "test")
    compile ++ test ++ compiler ++ internal
  }

  val Server: Seq[ModuleID] = {
    val compile = Seq(
      jsonpath,
      logbackClassic,
      scopt,
      zio,
      zioAsyncHttpBackend,
      zioConfig,
      zioConfigMagnolia,
      zioConfigTypesafe,
      zioLogging,
      zioLoggingSlf4j,
      zioStreams
    )
    val test = Seq(
      zioTest,
      zioTestMagnolia,
      zioTestSbt
    ) map (_ % "test")
    compile ++ test ++ compiler ++ internal
  }

}
