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
