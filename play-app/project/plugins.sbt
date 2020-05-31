scalaVersion := "2.12.11"

lazy val hotReloadPlugin = RootProject(file("../.."))
lazy val root = project
  .in(file("."))
  .dependsOn(hotReloadPlugin)
  .settings(
    scalaVersion := "2.12.11",
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.2"),
    addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
  )
