package com.malliina.app

import cats.data.Kleisli
import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import org.http4s.CacheDirective.{`must-revalidate`, `no-cache`, `no-store`}
import org.http4s.{HttpRoutes, _}
import org.http4s.headers.`Cache-Control`
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Service extends IOApp {
  def apply(cs: ContextShift[IO]): Service = new Service()(cs)

  val app = Service(contextShift).router

  def server = for {
    s <- BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port = 9000, "0.0.0.0")
      .withHttpApp(app)
      .resource
  } yield s

  override def run(args: List[String]): IO[ExitCode] =
    server.use(_ => IO.never).as(ExitCode.Success)
}

class Service()(implicit cs: ContextShift[IO]) extends Implicits {
  val noCache = `Cache-Control`(`no-cache`(), `no-store`, `must-revalidate`)

  val html = AppHtml(true)
  val routes = HttpRoutes.of[IO] {
    case req @ GET -> Root / rest => ok(html.index.tags)
  }
  val router: Kleisli[IO, Request[IO], Response[IO]] = Router("/" -> routes).orNotFound

  private def ok[A](a: A)(implicit w: EntityEncoder[IO, A]) = Ok(a, noCache)
}
