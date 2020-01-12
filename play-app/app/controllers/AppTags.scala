package controllers

import com.malliina.app.TagPage
import scalatags.Text.all._

object AppTags {
  def index(msg: String) = TagPage(
    html(
      head(script(src := "http://localhost:8080/socket.js", defer)),
      body(h1(msg))
    )
  )
}
