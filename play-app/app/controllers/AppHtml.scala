package controllers

import com.malliina.app.TagPage
import com.malliina.live.LiveReload
import scalatags.Text.all._

object AppHtml {
  def apply(isHotReloaded: Boolean): AppHtml = new AppHtml(isHotReloaded)
}

class AppHtml(isHotReloaded: Boolean) {
  val empty: Modifier = ""

  def index(msg: String) = TagPage(
    html(
      head(
        if (isHotReloaded) script(src := LiveReload.script, defer)
        else empty
      ),
      body(
        h1(msg)
      )
    )
  )
}
