val app = project
  .in(file("."))
  .enablePlugins(LiveRevolverPlugin)
  .settings(
    organization := "com.malliina",
    version := "0.0.1",
    scalaVersion := "3.1.1",
    libraryDependencies ++= Seq("ember-server", "dsl", "circe").map { m =>
      "org.http4s" %% s"http4s-$m" % "0.23.16"
    } ++ Seq("classic", "core").map { m => "ch.qos.logback" % s"logback-$m" % "1.2.5" } ++ Seq(
      "org.slf4j" % "slf4j-api" % "1.7.35",
      "com.lihaoyi" %% "scalatags" % "0.12.0",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
