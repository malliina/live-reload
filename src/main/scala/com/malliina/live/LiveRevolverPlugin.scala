package com.malliina.live

import com.malliina.live.LiveReloadPlugin.autoImport.reloader
import sbt._
import spray.revolver.RevolverPlugin
import spray.revolver.RevolverPlugin.autoImport.{reStart, reStop}

object LiveRevolverPlugin extends AutoPlugin {
  override def requires = LiveReloadPlugin && RevolverPlugin

  object autoImport {}

  override def projectSettings = Seq(
    reStart := reStart.dependsOn(Def.task(reloader.value.start())).evaluated,
    reStop := reStop.dependsOn(Def.task(reloader.value.close())).value
  )
}
