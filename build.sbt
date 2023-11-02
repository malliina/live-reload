import scala.sys.process.Process

ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

val updateDocs = taskKey[Unit]("Updates README.md")

inThisBuild(
  Seq(
    scalaVersion := "2.12.18",
    organization := "com.malliina"
  )
)

val plugin = Project("live-reload", file("."))
  .enablePlugins(MavenCentralPlugin)
  .settings(
    sbtPlugin := true,
    organization := "com.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    libraryDependencies ++= Seq("ember-server", "dsl").map { m =>
      "org.http4s" %% s"http4s-$m" % "0.23.23"
    } ++ Seq(
      "io.circe" %% "circe-generic" % "0.14.6"
    ),
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")
  )

val docs = project
  .in(file("mdoc"))
  .dependsOn(plugin)
  .enablePlugins(MdocPlugin)
  .settings(
    publish / skip := true,
    mdocVariables := Map("VERSION" -> version.value),
    mdocExtraArguments += "--no-link-hygiene",
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

plugin / beforeCommitRelease := (docs / updateDocs).value
