package com.scalawilliam.rad4s.http4s

import org.http4s._
import cats.{Functor, Monad}
import cats.data.{Kleisli, OptionT}

object NestedHttpRoutes {

  def lift[F[_]: Monad](
      nestedRoutes: Request[F] => HttpRoutes[F]
  ): HttpRoutes[F] =
    Kleisli { request =>
      nestedRoutes(request)(request)
    }

  def liftF[F[_]: Monad](
      nestedRoutes: Kleisli[F, Request[F], HttpRoutes[F]]
  ): HttpRoutes[F] =
    Kleisli { request =>
      OptionT
        .liftF(nestedRoutes(request))
        .flatMap(_(request))
    }

  def of[F[_]: Monad](
      pf: PartialFunction[Request[F], HttpRoutes[F]]
  ): HttpRoutes[F] = Kleisli { req =>
    OptionT
      .fromOption(pf.lift(req))
      .flatMap(_.apply(req))
  }

  def appToRoute[F[_]: Functor](httpApp: HttpApp[F]): HttpRoutes[F] =
    httpApp.mapK(OptionT.liftK)

}
