val app = project
  .in(file("."))
  .enablePlugins(PlayLiveReloadPlugin)
  .settings(
    organization := "com.malliina",
    version := "0.0.1",
    scalaVersion := "2.13.2",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.9.1",
      "org.scalameta" %% "munit" % "0.7.7" % Test
    )
  )
