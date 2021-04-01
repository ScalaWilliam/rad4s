/*
 * Copyright 2021 ScalaWilliam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scalawilliam.rad4s.http4s

import cats.data.Kleisli
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, IO, Resource}
import cats.implicits._
import org.http4s.server.ServiceErrorHandler
import org.http4s.servlet.{AsyncHttp4sServlet, ServletIo}
import org.http4s.{HttpApp, Request}

import javax.servlet.ServletConfig
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
