import scala.sys.process.Process

ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

val updateDocs = taskKey[Unit]("Updates README.md")

val http4sVersion = "0.23.3"

val http4sModules = Seq(
  "blaze-server",
  "blaze-client",
  "dsl",
  "circe"
)

val plugin = Project("live-reload", file("."))
  .enablePlugins(MavenCentralPlugin)
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.14",
    organization := "com.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies ++= http4sModules.map { m =>
      "org.http4s" %% s"http4s-$m" % http4sVersion
    } ++ Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.2.5" % Compile,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.2.5" % Provided
    ),
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
  )

val docs = project
  .in(file("mdoc"))
  .settings(
    scalaVersion := "2.12.14",
    crossScalaVersions -= "2.13.6",
    publish / skip := true,
    mdocVariables := Map("VERSION" -> version.value),
    mdocOut := (ThisBuild / baseDirectory).value,
    updateDocs := {
      val log = streams.value.log
      val outFile = mdocOut.value
      IO.relativize((ThisBuild / baseDirectory).value, outFile)
        .getOrElse(sys.error(s"Strange directory: $outFile"))
      val addStatus = Process(s"git add $outFile").run(log).exitValue()
      if (addStatus != 0) {
        sys.error(s"Unexpected status code $addStatus for git commit.")
      }
    },
    updateDocs := updateDocs.dependsOn(mdoc.toTask("")).value
  )
  .dependsOn(plugin)
  .enablePlugins(MdocPlugin)
