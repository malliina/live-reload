import scala.sys.process.Process

ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

val updateDocs = taskKey[Unit]("Updates README.md")

val plugin = Project("live-reload", file("."))
  .enablePlugins(MavenCentralPlugin)
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.17",
    organization := "com.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    libraryDependencies ++= Seq("ember-server", "dsl").map { m =>
      "org.http4s" %% s"http4s-$m" % "0.23.16"
    } ++ Seq(
      "io.circe" %% "circe-generic" % "0.14.3"
    ),
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
  )

val docs = project
  .in(file("mdoc"))
  .settings(
    scalaVersion := "2.12.17",
    crossScalaVersions -= "2.13.10",
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

plugin / beforeCommitRelease := (docs / updateDocs).value
