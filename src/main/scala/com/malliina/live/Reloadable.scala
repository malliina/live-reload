package com.malliina.live

import com.comcast.ip4s.{Host, IpLiteralSyntax, Port}

import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference

trait Reloadable extends Closeable {
  def isEnabled: Boolean
  def start(): Unit
  def host: Host
  def port: Port
  def scriptUrl: String
  def wsUrl: String
  def reload(): Unit = emit(SimpleEvent.reload)
  def emit(message: BrowserEvent): Unit
  def close(): Unit
  def httpUrl = s"http://$host:$port"
}

class OnOffReloadable(on: => Reloadable, off: Reloadable = NoopReloadable) extends Reloadable {
  private val server = new AtomicReference[Option[Reloadable]](None)
  override def isEnabled: Boolean = active.isEnabled
  override def start(): Unit = {
    if (server.get().isEmpty) {
      server.set(Option(on))
    }
  }
  override def host = active.host
  override def port = active.port
  override def scriptUrl = active.scriptUrl
  override def wsUrl = active.wsUrl
  override def emit(message: BrowserEvent): Unit = active.emit(message)
  override def close(): Unit = {
    val old = server.getAndSet(None)
    old.foreach(_.close())
  }
  private def active: Reloadable = server.get().getOrElse(off)
}

object NoopReloadable extends Reloadable {
  override def isEnabled: Boolean = false
  override def start(): Unit = ()
  override def host = host"localhost"
  override def port = port"12345"
  override def scriptUrl: String = s"http://$host:$port/script.js"
  override def wsUrl: String = s"ws://$host:$port/events"
  override def emit(message: BrowserEvent): Unit = ()
  override def close(): Unit = ()
}
