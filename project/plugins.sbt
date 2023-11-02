scalaVersion := "2.12.18"

Seq(
  "com.malliina" % "sbt-utils-maven" % "1.6.28",
  "org.scalameta" % "sbt-scalafmt" % "2.5.2",
  "org.scalameta" % "sbt-mdoc" % "2.4.0"
) map addSbtPlugin
