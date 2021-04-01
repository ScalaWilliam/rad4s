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

import cats.data.{Kleisli, OptionT}
import cats.{Functor, Monad}
import org.http4s._

object NestedHttpRoutes {

  def lift[F[_]](
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
