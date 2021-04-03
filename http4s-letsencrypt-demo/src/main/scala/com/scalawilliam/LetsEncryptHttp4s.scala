package com.scalawilliam

import cats.effect.{ExitCode, IO, IOApp}
import com.scalawilliam.fs2.letsencrypt.LetsEncryptFS2
import org.http4s.HttpRoutes
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.defaults

import scala.concurrent.ExecutionContext

object LetsEncryptHttp4s extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      crypto <- LetsEncryptFS2
        .fromEnvironment[IO]
        .flatMap(_.sslContextResource[IO])
      server <- BlazeServerBuilder
        .apply[IO](ExecutionContext.global)
        .withSslContext(crypto)
        .bindHttp(
          port = sys.env
            .get("HTTP_PORT")
            .flatMap(_.toIntOption)
            .getOrElse(defaults.HttpPort),
          host = sys.env.getOrElse("HTTP_HOST", defaults.Host)
        )
        .withHttpApp({
          import org.http4s.dsl.io._
          HttpRoutes.of[IO] {
            case req =>
              Ok(s"Hello! Here is your request: ${req}")
          }
        }.orNotFound)
        .resource
    } yield server
  }.use(_ => IO.never).as(ExitCode.Success)
}
