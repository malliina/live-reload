package com.malliina.live

import java.nio.charset.StandardCharsets
import sbt.Keys._
import sbt._

object LiveReloadPlugin extends AutoPlugin {
  object autoImport {
    val reloader = settingKey[Reloadable]("Interface to browsers")
    val liveReloadHost = settingKey[String]("Host for live reload, defaults to localhost")
    val liveReloadPort = settingKey[Int]("HTTP port for live reload, defaults to 10101")
    val refreshBrowsers = taskKey[Unit]("Refreshes browsers")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    liveReloadHost := "localhost",
    liveReloadPort := 10101,
    reloader := StaticServer.start(
      io.Path.userHome.toPath.resolve(".live-reload"),
      liveReloadHost.value,
      liveReloadPort.value,
      sLog.value
    ),
    onUnload in Global := (onUnload in Global).value andThen { state: State =>
      sLog.value.info("onUnload")
      reloader.value.close()
      state
    },
    refreshBrowsers := reloader.value.reload(),
    refreshBrowsers := refreshBrowsers.triggeredBy(Compile / compile).value,
    extraLoggers := {
      // Sends compilation log output to the browser
      // https://www.scala-sbt.org/1.x/docs/Howto-Logging.html#Add+a+custom+logger
      import org.apache.logging.log4j.core.LogEvent
      import org.apache.logging.log4j.core.appender.AbstractAppender
      import org.apache.logging.log4j.message.{Message, ObjectMessage}
      import sbt.internal.util.StringEvent

      class BrowserConsoleAppender(key: ScopedKey[_])
          extends AbstractAppender(
            "BrowserAppender", // name : String
            null, // filter : org.apache.logging.log4j.core.Filter
            null, // layout : org.apache.logging.log4j.core.Layout[ _ <: Serializable]
            false // ignoreExceptions : Boolean
          ) {

        this.start() // the log4j2 Appender must be started, or it will fail with an Exception

        override def append(event: LogEvent): Unit = {
          val message = {
            def forUnexpected(message: Message) =
              MessageEvent.log("error", s"Unexpected: ${message.getFormattedMessage}")

            event.getMessage match {
              case om: ObjectMessage => // what we expect
                om.getParameter match {
                  case se: StringEvent => MessageEvent.log(se.level, se.message)
                  case other           => forUnexpected(om)
                }
              case unexpected: Message => forUnexpected(unexpected)
            }
          }
          reloader.value.emit(message)
        }
      }

      val currentFunction = extraLoggers.value
      (key: ScopedKey[_]) => {
        new BrowserConsoleAppender(key) +: currentFunction(key)
      }
    },
    Compile / sourceGenerators += Def.task {
      val dest = (Compile / sourceManaged).value
      makeSources(dest, reloader.value)
    }.taskValue
  )

  def makeSources(destBase: File, server: Reloadable): Seq[File] = {
    val packageName = "com.malliina.live"
    val host = s"http://localhost:${server.port}"
    val content =
      s"""
         |package $packageName
         |
         |object LiveReload {
         |  val host = "$host"
         |  val script = "${server.scriptUrl}"
         |  val socket = "${server.wsUrl}"
         |}
      """.stripMargin.trim + IO.Newline
    val destFile = destDir(destBase, packageName) / s"LiveReload.scala"
    IO.write(destFile, content, StandardCharsets.UTF_8)
    Seq(destFile)
  }

  def destDir(base: File, packageName: String): File =
    packageName.split('.').foldLeft(base)((acc, part) => acc / part)
}
