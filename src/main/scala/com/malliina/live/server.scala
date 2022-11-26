package com.malliina.live

import cats.data.NonEmptyList
import cats.effect.kernel.Temporal
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxApplicativeId
import com.comcast.ip4s.{Host, IpLiteralSyntax, Port}
import fs2.Pipe
import fs2.concurrent.Topic
import fs2.concurrent.Topic.Closed
import fs2.io.file.Path as FS2Path
import io.circe.syntax.EncoderOps
import org.http4s.CacheDirective.`no-cache`
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Cache-Control`, `Content-Type`}
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.server.{Router, Server}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text
import sbt.util.Logger

import java.nio.file.Path
import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

trait Implicits extends syntax.AllSyntax with Http4sDsl[IO]

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

class Service(
  root: FS2Path,
  val host: Host,
  val port: Port,
  events: Topic[IO, BrowserEvent]
) extends Implicits {
  val supportedStaticExtensions = List(".html", ".js", ".map", ".css", ".png", ".ico")
  val scriptPath = "script.js"
  val scriptUrl: String = s"http://$host:$port/$scriptPath"
  val eventsPath = "events"
  val wsUrl: String = s"ws://$host:$port/$eventsPath"
  val script = resourceToString("script.js").replaceFirst("@WS_URL@", wsUrl)
  val cacheHeaders = NonEmptyList.of(`no-cache`())

  def routes(builder: WebSocketBuilder2[IO]) = HttpRoutes.of[IO] {
    case GET -> Root / `scriptPath` =>
      Ok(script).map(_.withContentType(`Content-Type`(MediaType.text.javascript)))
    case GET -> Root / `eventsPath` =>
      val fromClient: Pipe[IO, WebSocketFrame, Unit] = _.evalMap {
        case Text(message, _) => IO(println(message))
        case _                => IO.unit
      }
      builder.build(
        (fs2.Stream.emit(SimpleEvent.ping) ++ events.subscribe(100)).map(message =>
          Text(message.asJson.noSpaces)
        ),
        fromClient
      )
    case req @ GET -> rest =>
      val segments = rest.segments
      val file = if (segments.isEmpty) "index.html" else segments.mkString("/")
      (findFile(file, req) orElse findFile(s"$file.html", req))
        .map(_.putHeaders(`Cache-Control`(cacheHeaders)))
        .fold(notFound(req))(_.pure[IO])
        .flatten
  }

  def findFile(file: String, req: Request[IO]) =
    StaticFile.fromPath(root.resolve(file), Option(req))

  def router(builder: WebSocketBuilder2[IO]) = Router("/" -> routes(builder)).orNotFound

  def send(message: BrowserEvent): IO[Either[Closed, Unit]] = events.publish1(message)

  def notFound(req: Request[IO]) =
    NotFound(s"Not found: ${req.uri}.")

  private def resourceToString(path: String) = {
    val src = scala.io.Source.fromResource(path, getClass.getClassLoader)
    try src.getLines().mkString("\n")
    finally src.close()
  }
}

object StaticServer {
  val ec = ExecutionContext.global

  def apply(root: Path, log: Logger): StaticServer =
    new StaticServer(FS2Path.fromNioPath(root), log)
  def start(root: Path, host: Host, port: Port, log: Logger): Reloadable =
    apply(root, log).start(host, port)
}

class StaticServer(root: FS2Path, log: Logger)(implicit t: Temporal[IO]) {
  def server(host: Host, port: Port): Resource[IO, (Server, Service)] = for {
    topic <- Resource.eval(Topic[IO, BrowserEvent])
    service = new Service(root, host, port, topic)
    server <- EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpWebSocketApp(builder => service.router(builder))
      .withShutdownTimeout(1.millis)
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
        log.info(s"Sending ${message.asJson} to any browsers...")
        svc.send(message).unsafeRunSync()
      }
      override def close(): Unit = {
        log.info(s"Stopping server at $describe...")
        stopper.unsafeRunSync()
      }
    }
  }
}
