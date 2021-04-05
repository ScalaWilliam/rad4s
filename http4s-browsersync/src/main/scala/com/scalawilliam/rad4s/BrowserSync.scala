package com.scalawilliam.rad4s

import cats._
import cats.implicits._
import cats.effect.Sync
import org.typelevel.log4cats.Logger

import java.net.URI
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest}

object BrowserSync {

  def logging[F[_]: Sync](logger: Logger[F]): F[Unit] =
    logger.debug(s"Pinging BrowserSync") *> BrowserSync
      .ping[F]
      .flatMap {
        case Left(_: java.net.ConnectException) =>
          logger.error("Failed to connect to BrowserSync")
        case Left(reason) =>
          logger.error(reason)(s"Failed to ping BrowserSync: ${reason}")
        case Right(_) => logger.info("Pinged BrowserSync")
      }

  def conditionalOnEnvironment[F[_]: Sync](logger: Logger[F]): F[Unit] =
    checkIfRunBrowserSync[F].flatMap(doRun =>
      if (doRun) logging(logger) else Sync[F].unit)

  val EnvironmentVariableName = "PING_BROWSER_SYNC"
  val SystemPropertyName      = "ping.browser.sync"
  private val TruthyValues    = Set("1", "true", "yes")

  private def checkIfRunBrowserSync[F[_]: Sync]: F[Boolean] =
    Sync[F].delay {
      sys.props
        .get(SystemPropertyName)
        .orElse {
          sys.env
            .get(EnvironmentVariableName)
        }
        .map(_.toLowerCase)
        .exists(str => TruthyValues.contains(str))
    }

  private def ping[F[_]](
      implicit syncF: Sync[F]): F[Either[Throwable, String]] =
    syncF.attempt {
      syncF.delay {
        HttpClient
          .newBuilder()
          .version(HttpClient.Version.HTTP_1_1)
          .build()
          .send(
            HttpRequest.newBuilder
              .uri(
                URI.create(
                  "http://localhost:3000/__browser_sync__?method=reload",
                ))
              .build,
            BodyHandlers.ofString,
          )
          .body()
      }
    }
}
