scalaVersion := "2.12.10"

classpathTypes += "maven-plugin"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += Resolver.bintrayIvyRepo("malliina", "sbt-plugins")

Seq(
  "com.malliina" %% "sbt-utils-bintray" % "0.15.1",
  "ch.epfl.scala" % "sbt-bloop" % "1.4.0-RC1",
  "org.scalameta" % "sbt-scalafmt" % "2.3.0"
) map addSbtPlugin
