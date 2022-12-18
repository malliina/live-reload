package com.malliina.live

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global

import com.comcast.ip4s.{Host, Port}
import fs2.concurrent.Topic
import fs2.io.file.{Path => FS2Path}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import sbt.util.Logger

import java.nio.file.Path
import scala.concurrent.duration.DurationInt

import io.circe.syntax.EncoderOps

object StaticServer {
  def apply(root: Path, log: Logger): StaticServer =
    new StaticServer(FS2Path.fromNioPath(root), log)

  def start(root: Path, host: Host, port: Port, log: Logger): Reloadable =
    apply(root, log).start(host, port)
}

class StaticServer(root: FS2Path, log: Logger) {
  def server(host: Host, port: Port): Resource[IO, (Server, Service)] = for {
    topic <- Resource.eval(Topic[IO, BrowserEvent])
    service = new Service(root, host, port, topic)
    server <- EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpWebSocketApp(builder => service.router(builder))
      .withShutdownTimeout(1.millis)
      .withIdleTimeout(30.days)
      .build
  } yield (server, service)

  def start(serverHost: Host, serverPort: Port): Reloadable = {
    val describe = s"$serverHost:$serverPort"
    log.info(s"Starting live reload server at $describe...")
    val ((_, svc), stopper) = server(serverHost, serverPort).allocated.unsafeRunSync()
    log.info(s"Live reload server started at $describe.")
    new Reloadable {
      override def isEnabled: Boolean = true
      override def start(): Unit = ()
      override def host: Host = svc.host
      override def port: Port = svc.port
      override def scriptUrl: String = svc.scriptUrl
      override def wsUrl: String = svc.wsUrl
      override def emit(message: BrowserEvent): Unit = {
        log.info(s"Sending ${message.asJson.noSpaces} to any browsers...")
        svc.send(message).unsafeRunSync()
      }
      override def close(): Unit = {
        log.info(s"Stopping server at $describe...")
        stopper.unsafeRunSync()
      }
    }
  }
}
