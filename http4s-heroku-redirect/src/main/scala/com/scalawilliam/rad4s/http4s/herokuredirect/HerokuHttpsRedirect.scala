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

package com.scalawilliam.rad4s.http4s.herokuredirect

import cats.data._
import cats.effect.IO
import org.http4s.Uri.RegName
import org.http4s.dsl.io._
import org.http4s.headers.{Host, Location}
import org.http4s.util.CaseInsensitiveString
import org.http4s.{HttpRoutes, Request, Uri}

object HerokuHttpsRedirect {
  val HeaderName = "X-Forwarded-Proto"
  val WhenSsl    = "https"
  def isSecure(request: Request[IO]): Boolean =
    request.headers
      .get(CaseInsensitiveString(HeaderName))
      .exists(_.value == WhenSsl)

  def hostToUri(hostHeader: Host): Uri =
    Uri.apply(
      scheme = Some(Uri.Scheme.https),
      authority = Some(
        Uri.Authority(userInfo = None,
                      port = None,
                      host = RegName(hostHeader.host)))
    )

  def apply(targetUri: Uri)(route: HttpRoutes[IO]): HttpRoutes[IO] =
    Kleisli { req: Request[IO] =>
      if (isSecure(req))
        route(req)
      else
        OptionT.liftF {
          req.headers
            .get(Host)
            .fold(Forbidden("No Host header found"))(header =>
              SeeOther(Location(hostToUri(header))))
        }
    }
}
