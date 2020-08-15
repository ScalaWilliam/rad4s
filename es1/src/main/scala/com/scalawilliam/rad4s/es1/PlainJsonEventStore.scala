package com.scalawilliam.rad4s.es1

import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.generic.auto._
import java.nio.file.{Files, Path}
import cats._
import cats.implicits._
import cats.FlatMap
import cats.effect.Sync
import com.scalawilliam.rad4s.es1.EventStore.Event

final case class PlainJsonEventStore[F[_]](path: Path)(implicit F: Sync[F])
    extends EventStore[F] {
  override def putEvent(event: Event): F[Unit] =
    listEvents
      .flatMap(es =>
        F.delay { Files.writeString(path, (es :+ event).asJson.noSpaces) })
      .void

  override def listEvents: F[List[Event]] =
    F.delay {
      if (!Files.exists(path)) List.empty
      else if (Files.size(path) == 0) List.empty
      else
        decode[List[Event]](Files.readString(path))
          .getOrElse(sys.error(s"Cannot parse"))
    }
}
