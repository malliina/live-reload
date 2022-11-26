package com.malliina.live

import io.circe.{Codec, Encoder}
import io.circe.generic.semiauto.deriveCodec
import io.circe.syntax.EncoderOps

trait BrowserEvent {
  def event: String
}

object BrowserEvent {
  implicit val encoder: Encoder[BrowserEvent] = {
    case me @ MessageEvent(_, _, _) => me.asJson
    case se @ SimpleEvent(_)        => se.asJson
  }
}

case class MessageEvent(event: String, level: String, message: String) extends BrowserEvent

object MessageEvent {
  implicit val codec: Codec[MessageEvent] = deriveCodec[MessageEvent]

  def log(level: String, message: String) = apply("log", level, message)
}

case class SimpleEvent(event: String) extends BrowserEvent

object SimpleEvent {
  implicit val codec: Codec[SimpleEvent] = deriveCodec[SimpleEvent]
  val ping = SimpleEvent("ping")
  val reload = SimpleEvent("reload")
}
