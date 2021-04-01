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

package com.scalawilliam.rad4s.es1

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.concurrent.Ref
import com.scalawilliam.rad4s.es1.EventStore.Event

final case class RefMemoryStore[F[_]: Sync](ref: Ref[F, List[EventStore.Event]])
    extends EventStore[F] {
  override def putEvent(event: Event): F[Unit] =
    ref.update(items => items :+ event).void

  override def listEvents: F[List[Event]] = ref.get
}
