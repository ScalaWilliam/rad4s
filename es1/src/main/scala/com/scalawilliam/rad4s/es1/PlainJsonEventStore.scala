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

import cats.effect.Sync
import cats.implicits._
import com.scalawilliam.rad4s.es1.EventStore.Event
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import java.nio.file.{Files, Path}

final case class PlainJsonEventStore[F[_]](path: Path)(implicit F: Sync[F])
    extends EventStore[F] {
  override def putEvent(event: Event): F[Unit] =
    listEvents
      .flatMap(es =>
        F.delay {
          Files.write(path, (es :+ event).asJson.noSpaces.getBytes("UTF-8"))
      })
      .void

  override def listEvents: F[List[Event]] =
    F.delay {
      if (!Files.exists(path)) List.empty
      else if (Files.size(path) == 0) List.empty
      else
        decode[List[Event]](new String(Files.readAllBytes(path), "UTF-8"))
          .getOrElse(sys.error(s"Cannot parse"))
    }
}
