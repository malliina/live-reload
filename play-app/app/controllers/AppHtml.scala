package controllers

import com.malliina.app.TagPage
import scalatags.Text.all._

object AppHtml {
  def apply(isHotReloaded: Boolean): AppHtml = new AppHtml(isHotReloaded)
}

class AppHtml(isHotReloaded: Boolean) {
  val empty: Modifier = ""

  def index(msg: String) = TagPage(
    html(
      head(
        if (isHotReloaded) script(src := "http://localhost:8080/socket.js", defer)
        else empty
      ),
      body(
        h1(msg)
      )
    )
  )
}
