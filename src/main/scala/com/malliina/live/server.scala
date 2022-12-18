package com.malliina.live

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import com.comcast.ip4s.{Host, Port}
import fs2.Pipe
import fs2.concurrent.Topic
import fs2.concurrent.Topic.Closed
import fs2.io.file.{Path => FS2Path}
import io.circe.syntax.EncoderOps
import org.http4s.CacheDirective.`no-cache`
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Cache-Control`, `Content-Type`}
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.server.Router
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text
import scala.concurrent.duration.DurationInt

trait Implicits extends syntax.AllSyntax with Http4sDsl[IO]

class Service(
  root: FS2Path,
  val host: Host,
  val port: Port,
  events: Topic[IO, BrowserEvent]
) extends Implicits {
  private val scriptPath = "script.js"
  val scriptUrl: String = s"http://$host:$port/$scriptPath"
  private val eventsPath = "events"
  val wsUrl: String = s"ws://$host:$port/$eventsPath"
  val script = resourceToString("script.js").replaceFirst("@WS_URL@", wsUrl)
  private val cacheHeaders = NonEmptyList.of(`no-cache`())

  private val pings = fs2.Stream.awakeEvery[IO](30.seconds).map(d => SimpleEvent.ping)

  def routes(builder: WebSocketBuilder2[IO]) = HttpRoutes.of[IO] {
    case GET -> Root / `scriptPath` =>
      Ok(script).map(_.withContentType(`Content-Type`(MediaType.text.javascript)))
    case GET -> Root / `eventsPath` =>
      val fromClient: Pipe[IO, WebSocketFrame, Unit] = _.evalMap {
        case Text(message, _) => IO(println(message))
        case _                => IO.unit
      }
      builder.build(
        events
          .subscribe(100)
          .mergeHaltBoth(pings)
          .map(message => Text(message.asJson.noSpaces)),
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

  private def findFile(file: String, req: Request[IO]) =
    StaticFile.fromPath(root.resolve(file), Option(req))

  def router(builder: WebSocketBuilder2[IO]) = Router("/" -> routes(builder)).orNotFound

  def send(message: BrowserEvent): IO[Either[Closed, Unit]] = events.publish1(message)

  private def notFound(req: Request[IO]) =
    NotFound(s"Not found: ${req.uri}.")

  private def resourceToString(path: String) = {
    val src = scala.io.Source.fromResource(path, getClass.getClassLoader)
    try src.getLines().mkString("\n")
    finally src.close()
  }
}
