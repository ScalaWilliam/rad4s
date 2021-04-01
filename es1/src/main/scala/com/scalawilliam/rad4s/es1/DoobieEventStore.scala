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
import doobie._
import doobie.implicits._
import doobie.implicits.legacy.instant._

final case class DoobieEventStore[F[_]: Sync](tableName: String,
                                              xa: Transactor[F])
    extends EventStore[F] {
  private def tnf = Fragment.const(tableName)
  override def putEvent(event: EventStore.Event): F[Unit] = {
    import event._
    (fr"""INSERT INTO """ ++ tnf ++ fr""" (eventTime, eventName, eventData) VALUES ($eventTime, $eventName, $eventData)""").update.run
      .transact(xa)
      .void
  }

  override def listEvents: F[List[EventStore.Event]] =
    (fr"""SELECT eventTime, eventName, eventData FROM """ ++ tnf ++ fr""" ORDER BY eventTime ASC""")
      .query[EventStore.Event]
      .to[List]
      .transact(xa)
}
object DoobieEventStore {
  def forTable[F[_]: Sync](
      name: String): ConnectionIO[Transactor[F] => DoobieEventStore[F]] =
    (sql"""CREATE TABLE IF NOT EXISTS """ ++ Fragment.const(name) ++
      fr""" (eventTime TIMESTAMP PRIMARY KEY, eventName TEXT NOT NULL, eventData TEXT NOT NULL)
          """).update.run.void.as(trans => DoobieEventStore(name, trans))
}
