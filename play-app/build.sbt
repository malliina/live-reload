val app = project
  .in(file("."))
  .enablePlugins(PlayHotReloadPlugin)
  .settings(
    organization := "com.malliina",
    version := "0.0.1",
    scalaVersion := "2.13.1",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.8.3",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
    )
  )
