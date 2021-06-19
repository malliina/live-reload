package com.malliina.live

import java.awt.Desktop

import play.sbt.PlayRunHook
import sbt._

object ReloadHook {
  def apply(server: AkkaHttpReloadServer, openBrowserOnStart: Boolean, log: Logger): ReloadHook =
    new ReloadHook(server, openBrowserOnStart, log)
}

class ReloadHook(server: AkkaHttpReloadServer, openBrowserOnStart: Boolean, log: Logger)
    extends PlayRunHook {
  override def beforeStarted(): Unit = {
    server.start()
  }

  override def afterStarted(): Unit = {
    if (openBrowserOnStart)
      Desktop.getDesktop.browse(new URI("http://localhost:9000"))
  }

  override def afterStopped(): Unit = {
    server.close()
  }
}
