import scala.sys.process.Process

ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

val updateDocs = taskKey[Unit]("Updates README.md")

val plugin = Project("play-live-reload", file("."))
  .enablePlugins(MavenCentralPlugin)
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.11",
    organization := "com.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.12",
      "com.typesafe.akka" %% "akka-stream" % "2.6.5",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.2.5" % Compile,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.2.5" % Provided
    ),
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.2")
  )

val docs = project
  .in(file("mdoc"))
  .settings(
    scalaVersion := "2.12.11",
    crossScalaVersions -= "2.13.2",
    skip.in(publish) := true,
    mdocVariables := Map("VERSION" -> version.value),
    mdocOut := (baseDirectory in ThisBuild).value,
    updateDocs := {
      val log = streams.value.log
      val outFile = mdocOut.value
      IO.relativize((baseDirectory in ThisBuild).value, outFile)
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
