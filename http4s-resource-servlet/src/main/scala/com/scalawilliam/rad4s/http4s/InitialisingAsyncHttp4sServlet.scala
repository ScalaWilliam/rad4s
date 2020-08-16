package com.scalawilliam.rad4s.http4s

import cats.data.Kleisli
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, IO, Resource}
import cats.implicits._
import javax.servlet.ServletConfig
import org.http4s.server.ServiceErrorHandler
import org.http4s.servlet.{AsyncHttp4sServlet, ServletIo}
import org.http4s.{HttpApp, Request}

import scala.concurrent.duration.Duration

/** Servlet to allow for HttpApp to be opened as a resource, and depend on ServletConfig,
  * such as to pick up the context path for example. */
class InitialisingAsyncHttp4sServlet(
    appResource: ServletConfig => Resource[IO, HttpApp[IO]],
    serviceRef: Ref[IO, HttpApp[IO]] = Ref.unsafe(HttpApp.notFound),
    cleanUpRef: Ref[IO, IO[Unit]] = Ref.unsafe(IO.unit),
    asyncTimeout: Duration = Duration.Inf,
    private[this] var servletIo: ServletIo[IO],
    serviceErrorHandler: ServiceErrorHandler[IO]
)(implicit C: ConcurrentEffect[IO])
    extends AsyncHttp4sServlet[IO](
      Kleisli { (i: Request[IO]) =>
        serviceRef.get.flatMap(f => f.apply(i))
      },
      asyncTimeout,
      servletIo,
      serviceErrorHandler
    ) {

  override def init(config: ServletConfig): Unit = {
    super.init(config)
    val (app, close) = appResource(config).allocated.unsafeRunSync()
    cleanUpRef.set(close).unsafeRunSync()
    serviceRef.set(app).unsafeRunSync()
  }

  override def destroy(): Unit = {
    super.destroy()
    cleanUpRef.get.flatten.unsafeRunSync()
  }

}
