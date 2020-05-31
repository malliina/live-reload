ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

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
