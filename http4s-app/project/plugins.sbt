scalaVersion := "2.12.18"

lazy val liveReloadPlugin = RootProject(file("../.."))
lazy val root = project
  .in(file("."))
  .dependsOn(liveReloadPlugin)
  .settings(
    scalaVersion := "2.12.18",
    addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0"),
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0"),
    addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
  )
