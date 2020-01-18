package com.malliina.live

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

case class MessageEvent(event: String, level: String, message: String)

object MessageEvent {
  implicit val codec: JsonValueCodec[MessageEvent] =
    JsonCodecMaker.make[MessageEvent](CodecMakerConfig)

  def log(level: String, message: String) = apply("log", level, message)
}

case class SimpleEvent(event: String)

object SimpleEvent {
  implicit val codec: JsonValueCodec[SimpleEvent] =
    JsonCodecMaker.make[SimpleEvent](CodecMakerConfig)
  val ping = SimpleEvent("ping")
  val reload = SimpleEvent("reload")
}
