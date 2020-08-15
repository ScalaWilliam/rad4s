package com.scalawilliam.rad4s.es1

import java.nio.file.Path
import java.time.Instant

import cats.effect.Sync
import doobie.{ConnectionIO, Fragment, Transactor}

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

}
