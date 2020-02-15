package com.malliina.live

import java.util.concurrent.atomic.AtomicReference

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import com.github.plokhotnyuk.jsoniter_scala.core.writeToString
import com.typesafe.config.ConfigFactory
import sbt.io.IO
import sbt.util.Logger

import scala.concurrent.duration.DurationInt

object BrowserClient {
  def apply(host: String, port: Int, log: Logger): BrowserClient = {
    // Adapted from workbench
    val cl = getClass.getClassLoader
    val system = ActorSystem(
      "live-ws",
      config = ConfigFactory.load(cl),
      classLoader = cl
    )
    apply(host, port, log, cl, system)
  }
  def apply(host: String, port: Int, log: Logger, cl: ClassLoader, as: ActorSystem): BrowserClient =
    new BrowserClient(host, port, log, cl)(as)
}

class BrowserClient(host: String, val port: Int, log: Logger, cl: ClassLoader)(
    implicit as: ActorSystem
) {
  implicit val mat = ActorMaterializer()
  implicit val ec = as.dispatcher
  // Injects port to JavaScript template
  val script = resourceToString("script.js").replaceFirst("@PORT@", s"$port")

  private def resourceToString(path: String) = {
    val src = scala.io.Source.fromResource(path, cl)
    try src.getLines().mkString(IO.Newline)
    finally src.close()
  }

  val server = new AtomicReference[Option[Http.ServerBinding]](None)

  // https://doc.akka.io/docs/akka/current/stream/stream-dynamic.html
  val (eventSink, eventSource) =
    MergeHub
      .source[String](perProducerBufferSize = 16)
      .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
      .run()
  eventSource.runWith(Sink.ignore)
  val socketFlow: Flow[Message, Message, NotUsed] =
    Flow
      .fromSinkAndSource(
        Sink.ignore,
        eventSource.map(TextMessage(_))
      )
      .keepAlive(10.seconds, () => TextMessage(writeToString(SimpleEvent.ping)))
      .backpressureTimeout(3.seconds)

  val websocketRoute = concat(
    path("") {
      get {
        getFromResource("index.html")
      }
    },
    path("script.js") {
      val contentType = MediaTypes.`application/javascript`.withCharset(HttpCharsets.`UTF-8`)
      val entity = HttpEntity(contentType, script)
      complete(HttpResponse(status = StatusCodes.OK, entity = entity))
    },
    path("ws") {
      handleWebSocketMessages(socketFlow)
    }
  )

  def reload(): Unit = {
    Source.single(writeToString(SimpleEvent.reload)).to(eventSink).run()
  }

  def emitLog(message: MessageEvent) = {
    Source
      .single(writeToString(message))
      .to(eventSink)
      .run()
  }

  def start(): Unit = {
    Http()
      .bindAndHandle(websocketRoute, host, port)
      .map { http =>
        log.info(
          s"Server online at http://$host:$port/"
        )
        server.set(Option(http))
      }
      .recover {
        case _: Exception =>
          log.err("Failed to start HTTP server.")
      }
  }

  def close(): Unit = {
    log.info("Closing server...")
    server.get().foreach(_.unbind())
  }

  def terminate(): Unit = as.terminate()
}
