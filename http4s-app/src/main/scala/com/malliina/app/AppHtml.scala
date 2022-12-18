package com.malliina.app

import com.malliina.live.LiveReload
import scalatags.Text.all.*

class AppHtml(isHotReloaded: Boolean):
  val empty: Modifier = ""

  def index = page("Hello!")

  def page(msg: String) = TagPage(
    html(
      head(
        if isHotReloaded then script(src := LiveReload.script, defer)
        else empty
      ),
      body(
        h1(msg)
      )
    )
  )
