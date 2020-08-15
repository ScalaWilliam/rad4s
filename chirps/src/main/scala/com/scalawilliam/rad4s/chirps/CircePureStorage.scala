package com.scalawilliam.rad4s.chirps

import io.circe.syntax._
import io.circe.parser._
import java.nio.file.{Files, Path}

import cats._
import cats.implicits._
import cats.effect.concurrent.{Ref, MVar}
import cats.effect.{ContextShift, IO, Sync, Concurrent}
import io.circe.{Json, Encoder, Decoder}
import io.circe.jawn.JawnParser
import CircePureStorage._
import cats.effect.implicits._

object CircePureStorage {

  final class MLock[F[_]: Sync: Concurrent](mvar: MVar[F, Unit]) {
    def acquire: F[Unit] =
      mvar.take

    def release: F[Unit] =
      mvar.put(())

    def greenLight[A](fa: F[A]): F[A] =
      acquire.bracket(_ => fa)(_ => release)
  }

  object MLock {
    // todo PR to cats docs
    def apply[F[_]: Concurrent: Sync](
        implicit contextShift: ContextShift[F]): F[MLock[F]] =
      MVar[F].of(()).map(ref => new MLock(ref))
  }

  def make[F[_]: Sync, T: Encoder: Decoder](path: Path): PureStorage[F, T] =
    CircePureStorage(path)

  def ensureSafe[F[_]: Sync, T](locker: MLock[F])(
      pureStorage: PureStorage[F, T]): PureStorage[F, T] =
    new PureStorage[F, T] {
      override def modify(f: Option[T] => F[T]): F[T] =
        locker.greenLight(pureStorage.modify(f))

      override def read: F[Option[T]] = pureStorage.read
    }

  import cats.implicits._
  def fromRef[F[_]: Sync, T](ref: Ref[F, T]): PureStorage[F, T] =
    new PureStorage[F, T] {
      override def modify(f: Option[T] => F[T]): F[T] =
        ref.get.flatMap(v => f(Some(v))).flatMap(v => ref.set(v).as(v))

      override def read: F[Option[T]] =
        ref.get.map(Option.apply)
    }

}

final case class CircePureStorage[F[_], T: Encoder: Decoder](path: Path)(
    implicit F: Sync[F])
    extends PureStorage[F, T] {

  private def fetchFile: F[Option[Json]] = F.delay {
    if (Files.exists(path)) Some {
      JawnParser
        .apply(allowDuplicateKeys = false)
        .parseFile(path.toFile)
        .fold(throw _, identity)
    } else None
  }

  import cats.implicits._
  def modify(f: Option[T] => F[T]): F[T] =
    read.flatMap(f).flatTap(put)

  private def put(v: T): F[Unit] =
    F.delay(Files.writeString(path, v.asJson.spaces2)).void

  def read: F[Option[T]] =
    fetchFile.map(_.map(_.as[T].fold(throw _, identity)))
}
