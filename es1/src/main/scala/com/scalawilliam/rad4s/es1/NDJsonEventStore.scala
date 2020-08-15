package com.scalawilliam.rad4s.es1

import java.nio.file.{Files, Path, StandardOpenOption}

import cats._
import cats.implicits._
import cats.effect.Sync
import com.scalawilliam.rad4s.es1.EventStore.Event

final case class NDJsonEventStore[F[_]](path: Path)(implicit F: Sync[F])
    extends EventStore[F] {
  import io.circe._
  import io.circe.syntax._
  import io.circe.parser._
  import io.circe.generic.auto._
  override def putEvent(event: Event): F[Unit] =
    F.delay {
      Files.writeString(path,
                        event.asJson.noSpaces + "\n",
                        StandardOpenOption.APPEND)
    }.void

  override def listEvents: F[List[Event]] =
    F.delay {
      import scala.jdk.CollectionConverters._
      if (!Files.exists(path)) Nil
      else
        Files
          .readAllLines(path)
          .asScala
          .map(line =>
            decode[Event](line).getOrElse(sys.error(s"Cannot parse")))
          .toList
    }
}
