val http4sModules = Seq(
  "blaze-server",
  "blaze-client",
  "dsl",
  "circe"
)

val app = project
  .in(file("."))
  .enablePlugins(LiveRevolverPlugin)
  .settings(
    organization := "com.malliina",
    version := "0.0.1",
    scalaVersion := "3.0.2",
    libraryDependencies ++= http4sModules.map { m =>
      "org.http4s" %% s"http4s-$m" % "0.23.3"
    } ++ Seq("classic", "core").map { m => "ch.qos.logback" % s"logback-$m" % "1.2.5" } ++ Seq(
      "org.slf4j" % "slf4j-api" % "1.7.32",
      ("com.lihaoyi" %% "scalatags" % "0.9.4").cross(CrossVersion.for3Use2_13),
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
