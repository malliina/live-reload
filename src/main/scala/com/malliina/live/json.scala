package com.malliina.live

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

trait BrowserEvent {
  def event: String
  def asJson: String = this match {
    case me @ MessageEvent(_, _, _) => writeToString(me)
    case se @ SimpleEvent(_)        => writeToString(se)
  }
}

case class MessageEvent(event: String, level: String, message: String) extends BrowserEvent

object MessageEvent {
  implicit val codec: JsonValueCodec[MessageEvent] =
    JsonCodecMaker.make[MessageEvent](CodecMakerConfig)

  def log(level: String, message: String) = apply("log", level, message)
}

case class SimpleEvent(event: String) extends BrowserEvent

object SimpleEvent {
  implicit val codec: JsonValueCodec[SimpleEvent] =
    JsonCodecMaker.make[SimpleEvent](CodecMakerConfig)
  val ping = SimpleEvent("ping")
  val reload = SimpleEvent("reload")
}
