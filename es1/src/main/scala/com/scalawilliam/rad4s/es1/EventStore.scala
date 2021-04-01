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
import cats.effect.concurrent.Ref
import cats.implicits._
import doobie.{ConnectionIO, Transactor}

import java.nio.file.Path
import java.time.Instant

trait EventStore[F[_]] {
  def putEvent(event: EventStore.Event): F[Unit]
  def listEvents: F[List[EventStore.Event]]
}
object EventStore {
  final case class Event(eventTime: Instant,
                         eventName: String,
                         eventData: String)
  final case class EventTimeMs(eventTime: Long,
                               eventName: String,
                               eventData: String) {
    def toEvent: Event =
      Event(eventTime = Instant.ofEpochMilli(eventTime),
            eventName = eventName,
            eventData = eventData)
  }

  def ndJsonFileStore[F[_]: Sync](path: Path): EventStore[F] =
    new NDJsonEventStore[F](path)

  def jsonFileStore[F[_]: Sync](path: Path): EventStore[F] =
    PlainJsonEventStore(path)

  def forDoobieTable[F[_]: Sync](
      name: String): ConnectionIO[Transactor[F] => DoobieEventStore[F]] =
    DoobieEventStore.forTable[F](name)

  def inMemoryStore[F[_]: Sync](initial: List[Event]): F[EventStore[F]] =
    Ref.of[F, List[EventStore.Event]](initial).map(RefMemoryStore[F](_))

}
