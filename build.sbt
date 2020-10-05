import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.defaultUniversalScript

autoStartServer := false
Global / cancelable := true

def itFilter(name: String): Boolean   = name endsWith "ITSpec"
def testFilter(name: String): Boolean = (name endsWith "Spec") && !itFilter(name)

val IntegrationTestTag = Tags.Tag("it")

Global / concurrentRestrictions += Tags.limit(IntegrationTestTag, 1)

lazy val testSettings = Seq(
  testOptions in Test ++= Seq(Tests.Filter(testFilter))
)

lazy val itSettings =
  inConfig(IntegrationTest)(Defaults.testSettings) ++
    Seq(
      fork in IntegrationTest := true,
      parallelExecution in IntegrationTest := true,            // NOTE: parallelExecution controls whether tests are mapped to separate tasks
      tags in IntegrationTest := Seq((IntegrationTestTag, 1)), // NOTE: to restrict the number of concurrently executing given tests in all projects
      testOptions in IntegrationTest := Seq(Tests.Filter(itFilter)),
      scalaSource in IntegrationTest := baseDirectory.value / "src/test/scala",
      javaSource in IntegrationTest := baseDirectory.value / "src/test/java",
      resourceDirectory in IntegrationTest := baseDirectory.value / "src/test/resources"
    )

lazy val allSettings = Settings.shared ++ testSettings ++ itSettings

lazy val server = (project in file("server"))
  .settings(allSettings: _*)
  .settings(
    name := "server",
    libraryDependencies ++= Dependencies.Server
  )

lazy val doom = (project in file("doom"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(server)
  .settings(allSettings: _*)
  .settings(Settings.testZioSettings)
  .settings(Settings.assemblySettings)
  .settings(
    name := "doom",
    libraryDependencies ++= Dependencies.Doom,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.gchudnov.doom",
    mainClass in assembly := Some("com.github.gchudnov.doom.Doom"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultUniversalScript(shebang = true))),
    assemblyJarName in assembly := s"${name.value}"
  )

lazy val root = (project in file("."))
  .aggregate(doom, server)
  .settings(allSettings: _*)
  .settings(
    name := "doomsday-metrics"
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
