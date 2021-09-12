scalaVersion := "2.12.14"

Seq(
  "com.malliina" % "sbt-utils-maven" % "1.2.5",
  "org.scalameta" % "sbt-scalafmt" % "2.4.3",
  "org.scalameta" % "sbt-mdoc" % "2.2.23"
) map addSbtPlugin
