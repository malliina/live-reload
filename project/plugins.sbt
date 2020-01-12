scalaVersion := "2.12.10"

classpathTypes += "maven-plugin"

scalacOptions ++= Seq("-unchecked", "-deprecation")

Seq(
  "ch.epfl.scala" % "sbt-bloop" % "1.4.0-RC1",
  "org.scalameta" % "sbt-scalafmt" % "2.3.0"
) map addSbtPlugin
