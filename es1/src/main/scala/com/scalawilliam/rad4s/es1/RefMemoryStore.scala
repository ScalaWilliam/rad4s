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
