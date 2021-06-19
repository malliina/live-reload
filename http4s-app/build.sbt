val http4sVersion = "0.21.24"

val http4sModules = Seq(
  "blaze-server",
  "blaze-client",
  "dsl",
  "scalatags",
  "circe",
  "play-json"
)

val app = project
  .in(file("."))
  .enablePlugins(LiveReloadPlugin)
  .settings(
    organization := "com.malliina",
    version := "0.0.1",
    scalaVersion := "2.13.6",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies ++= http4sModules.map { m =>
      "org.http4s" %% s"http4s-$m" % http4sVersion
    } ++ Seq(
      "org.slf4j" % "slf4j-api" % "1.7.30",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "ch.qos.logback" % "logback-core" % "1.2.3",
      "com.lihaoyi" %% "scalatags" % "0.9.4",
      "org.scalameta" %% "munit" % "0.7.26" % Test
    )
  )
