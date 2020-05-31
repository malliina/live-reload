import scala.sys.process.Process

val app = project
  .in(file("."))
  .enablePlugins(PlayLiveReloadPlugin)
  .settings(
    organization := "com.malliina",
    version := "0.0.1",
    scalaVersion := "2.13.2",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.9.1",
      "org.scalameta" %% "munit" % "0.7.7" % Test
    )
  )

val docs = project
  .in(file("mdoc"))
  .settings(
    scalaVersion := "2.12.11",
    crossScalaVersions -= "2.13.2",
    skip.in(publish) := true,
    mdocVariables := Map("VERSION" -> version.value),
    mdocOut := (baseDirectory in ThisBuild).value,
    updateDocs := {
      val log = streams.value.log
      val outFile = mdocOut.value
      IO.relativize((baseDirectory in ThisBuild).value, outFile)
        .getOrElse(sys.error(s"Strange directory: $outFile"))
      val addStatus = Process(s"git add $outFile").run(log).exitValue()
      if (addStatus != 0) {
        sys.error(s"Unexpected status code $addStatus for git commit.")
      }
    },
    updateDocs := updateDocs.dependsOn(mdoc.toTask("")).value
  )
  .dependsOn(mobilePush)
  .enablePlugins(MdocPlugin)
