package com.malliina.hot

import sbt.Keys.{compile, extraLoggers, sLog}
import sbt.{AutoPlugin, Compile, ScopedKey, settingKey, taskKey}

object HotReloadPlugin extends AutoPlugin {
  object autoImport {
    val reloader = settingKey[BrowserClient]("Interface to browsers")
    val refreshBrowsers = taskKey[Unit]("Refreshes browsers")
  }
  import autoImport._

  override def projectSettings = Seq(
    reloader := BrowserClient(sLog.value),
    refreshBrowsers := reloader.value.reload(),
    refreshBrowsers := refreshBrowsers.triggeredBy(compile in Compile).value,
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
          reloader.value.emitLog(message)
        }
      }

      val currentFunction = extraLoggers.value
      (key: ScopedKey[_]) => {
        new BrowserConsoleAppender(key) +: currentFunction(key)
      }
    }
  )
}
