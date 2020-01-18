ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

Global / useGpg := true

val plugin = Project("play-live-reload", file("."))
  .enablePlugins(BintrayReleasePlugin)
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.10",
    organization := "com.malliina",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.3",
      "com.typesafe.akka" %% "akka-stream" % "2.5.14",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.1.2" % Compile,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.1.2" % Provided
    ),
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.0")
  )
