scalaVersion := "2.12.14"

lazy val liveReloadPlugin = RootProject(file("../.."))
lazy val root = project
  .in(file("."))
  .dependsOn(liveReloadPlugin)
  .settings(
    scalaVersion := "2.12.14",
    addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0"),
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1"),
    addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.3")
  )
