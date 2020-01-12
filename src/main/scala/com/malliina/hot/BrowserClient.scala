package com.malliina.hot

import java.util.concurrent.atomic.AtomicReference

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import com.typesafe.config.ConfigFactory
import sbt.util.Logger
import com.github.plokhotnyuk.jsoniter_scala.core.writeToString

import scala.concurrent.duration.DurationInt

object BrowserClient {
  def apply(log: Logger): BrowserClient = {
    // Adapted from workbench
    val cl = getClass.getClassLoader
    val system = ActorSystem(
      "hot-ws",
      config = ConfigFactory.load(cl),
      classLoader = cl
    )
    apply(log, system)
  }
  def apply(log: Logger, as: ActorSystem): BrowserClient = new BrowserClient(log)(as)
}

class BrowserClient(log: Logger)(implicit as: ActorSystem) {
  implicit val mat = ActorMaterializer()
  implicit val ec = as.dispatcher

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
    path("socket.js") {
      getFromResource("socket.js")
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
      .bindAndHandle(websocketRoute, "localhost", 8080)
      .map { http =>
        as.log.info("Server online.")
        log.info(
          s"Server online at http://${http.localAddress.getHostName}:${http.localAddress.getPort}/"
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
