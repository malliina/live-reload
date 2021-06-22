package com.malliina.live

import cats.data.NonEmptyList
import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxFlatten}
import fs2.Pipe
import fs2.concurrent.Topic
import org.http4s.CacheDirective.`no-cache`
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Cache-Control`, `Content-Type`}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.server.{Router, Server}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text
import org.http4s.{HttpRoutes, Request, StaticFile, _}
import sbt.util.Logger

import java.io.Closeable
import java.nio.file.{Path, Paths}
import scala.concurrent.ExecutionContext

trait Implicits extends syntax.AllSyntaxBinCompat with Http4sDsl[IO]

object NoopReloadable extends Reloadable {
  override def host = "localhost"
  override def port = 12345
  override def scriptUrl: String = s"http://$host:$port/script.js"
  override def wsUrl: String = s"ws://$host:$port/events"
  override def emit(message: BrowserEvent): Unit = ()
  override def close(): Unit = ()
}
trait Reloadable extends Closeable {
  def host: String
  def port: Int
  def scriptUrl: String
  def wsUrl: String
  def reload(): Unit = emit(SimpleEvent.reload)
  def emit(message: BrowserEvent): Unit
  def close(): Unit
  def httpUrl = s"http://$host:$port"
}

class Service(
    root: Path,
    val host: String,
    val port: Int,
    events: Topic[IO, BrowserEvent],
    blocker: Blocker
)(
    implicit cs: ContextShift[IO]
) extends Implicits {
  val supportedStaticExtensions = List(".html", ".js", ".map", ".css", ".png", ".ico")
  val scriptPath = "script.js"
  val scriptUrl: String = s"http://$host:$port/$scriptPath"
  val eventsPath = "events"
  val wsUrl: String = s"ws://$host:$port/$eventsPath"
  val script = resourceToString("script.js").replaceFirst("@WS_URL@", wsUrl)
  val cacheHeaders = NonEmptyList.of(`no-cache`())
  val routes = HttpRoutes.of[IO] {
    case GET -> Root / `scriptPath` =>
      Ok(script).map(_.withContentType(`Content-Type`(MediaType.text.javascript)))
    case GET -> Root / `eventsPath` =>
      val fromClient: Pipe[IO, WebSocketFrame, Unit] = _.evalMap {
        case Text(message, _) => IO(println(message))
        case _                => IO.unit
      }

      WebSocketBuilder[IO].build(
        (fs2.Stream.emit(SimpleEvent.ping) ++ events.subscribe(100).drop(1)).map(message =>
          Text(message.asJson)
        ),
        fromClient
      )
    case req @ GET -> rest =>
      val file = if (rest.toList.isEmpty) "index.html" else rest.toList.mkString("/")
      (findFile(file, req) orElse findFile(s"$file.html", req))
        .map(_.putHeaders(`Cache-Control`(cacheHeaders)))
        .fold(notFound(req))(_.pure[IO])
        .flatten
  }

  def findFile(file: String, req: Request[IO]) =
    StaticFile.fromFile(root.resolve(file).toFile, blocker, Option(req))

  val router = Router("/" -> routes).orNotFound

  def send(message: BrowserEvent) = events.publish1(message)

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
  val cs = IO.contextShift(ec)
  val timer = IO.timer(ec)

  def apply(root: Path, log: Logger): StaticServer = new StaticServer(root, log)(cs, timer)
  def start(root: Path, host: String, port: Int, log: Logger): Reloadable =
    apply(root, log).start(host, port)
}

class StaticServer(root: Path, log: Logger)(implicit cs: ContextShift[IO], t: Timer[IO]) {
  def server(host: String, port: Int): Resource[IO, (Server[IO], Service)] = for {
    blocker <- Blocker[IO]
    topic <- Resource.eval(Topic[IO, BrowserEvent](SimpleEvent.ping))
    service = new Service(root, host, port, topic, blocker)
    server <- BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port, host)
      .withHttpApp(service.router)
      .resource
  } yield (server, service)

  def start(serverHost: String, serverPort: Int): Reloadable = {
    val describe = s"$serverHost:$serverPort"
    log.info(s"Starting live reload server at $describe...")
    val ((_, svc), stopper) = server(serverHost, serverPort).allocated.unsafeRunSync()
    log.info(s"Live reload server started at $describe.")
    new Reloadable {
      override def host: String = svc.host
      override def port: Int = svc.port
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

trait ServerApp extends IOApp {
  val root = Paths.get(sys.props("user.home")).resolve("code/meny/target/site")
  val server: Resource[IO, Server[IO]] = ??? // new StaticServer(root).server()

  override def run(args: List[String]): IO[ExitCode] =
    server.use(_ => IO.never).as(ExitCode.Success)
}
