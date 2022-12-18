package com.malliina.live

import com.malliina.live.LiveReloadPlugin.autoImport.{refreshBrowsers, reloader}
import com.malliina.live.LiveRevolverPlugin.autoImport.startApp
import sbt.*
import spray.revolver.{AppProcess, RevolverPlugin}
import spray.revolver.RevolverPlugin.autoImport.{reStart, reStop}

object LiveRevolverPlugin extends AutoPlugin {
  override def requires = LiveReloadPlugin && RevolverPlugin

  object autoImport {
    val startApp = taskKey[AppProcess]("Starts app")
  }

  override def projectSettings = Seq(
    reStart := reStart.dependsOn(Def.task(reloader.value.start())).evaluated,
    reStop := reStop.dependsOn(Def.task(reloader.value.close())).value,
    startApp := reStart.toTask(" ").value,
    refreshBrowsers := refreshBrowsers.triggeredBy(startApp).value
  )
}
