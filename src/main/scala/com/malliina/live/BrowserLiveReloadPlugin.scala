package com.malliina.live

import sbt.{AutoPlugin, settingKey}

object BrowserLiveReloadPlugin extends AutoPlugin {
  override def requires = LiveReloadPlugin

  object autoImport {
    val openBrowserOnStart = settingKey[Boolean]("Open browser on Play app start")
  }
  import autoImport.openBrowserOnStart

  override def projectSettings = Seq(
    openBrowserOnStart := true
  )
}
