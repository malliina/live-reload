package com.malliina.hot

import com.malliina.hot.HotReloadPlugin.autoImport._
import play.sbt.PlayImport.PlayKeys
import play.sbt.PlayScala
import sbt.{AutoPlugin, settingKey}
import sbt.Keys._

object PlayHotReloadPlugin extends AutoPlugin {
  override def requires = HotReloadPlugin && PlayScala

  object autoImport {
    val openBrowserOnStart = settingKey[Boolean]("Open browser on Play app start")
  }
  import autoImport.openBrowserOnStart

  override def projectSettings = Seq(
    openBrowserOnStart := true,
    PlayKeys.playRunHooks += ReloadHook(reloader.value, openBrowserOnStart.value, sLog.value)
  )
}
