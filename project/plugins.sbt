scalaVersion := "2.12.17"

Seq(
  "com.malliina" % "sbt-utils-maven" % "1.2.15",
  "org.scalameta" % "sbt-scalafmt" % "2.5.0",
  "org.scalameta" % "sbt-mdoc" % "2.3.6"
) map addSbtPlugin
