package com.malliina.app

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{host, port}
import org.http4s.CacheDirective.{`must-revalidate`, `no-cache`, `no-store`}
import org.http4s.{HttpRoutes, *}
import org.http4s.headers.`Cache-Control`
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder

import concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext

object Service extends IOApp:
  val app = Service().router

  def server = EmberServerBuilder
    .default[IO]
    .withIdleTimeout(30.days)
    .withHost(host"0.0.0.0")
    .withPort(port"9000")
    .withHttpApp(app)
    .withShutdownTimeout(1.millis)
    .build

  override def run(args: List[String]): IO[ExitCode] =
    server.use(_ => IO.never).as(ExitCode.Success)

class Service extends Implicits:
  val noCache = `Cache-Control`(`no-cache`(), `no-store`, `must-revalidate`)

  val html = AppHtml(true)
  val routes = HttpRoutes.of[IO] { case req @ GET -> Root =>
    ok(html.index.tags)
  }
  val router: Kleisli[IO, Request[IO], Response[IO]] = Router("/" -> routes).orNotFound

  private def ok[A](a: A)(implicit w: EntityEncoder[IO, A]) = Ok(a, noCache)
