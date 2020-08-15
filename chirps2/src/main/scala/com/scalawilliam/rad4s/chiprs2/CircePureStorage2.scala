package com.scalawilliam.rad4s.chiprs2

import io.circe.syntax._
import io.circe.parser._
import java.nio.file.{Files, Path}

import cats.effect.concurrent.{Ref, MVar}
import cats.effect.Sync
import com.scalawilliam.rad4s.chiprs2.CircePureStorage2.PureStorage
import com.scalawilliam.rad4s.chirps.CircePureStorage.MLock
import io.circe.{Json, Encoder, Decoder}
import io.circe.jawn.JawnParser

object CircePureStorage2 {

  trait PureStorage[F[_], T] {
    def zero: T
    def modify(f: T => F[T]): F[T]
    def modifyPure(f: T => T)(implicit F: Sync[F]): F[T] =
      modify(i => F.delay(f(i)))
    def read: F[T]
  }

  def make[F[_]: Sync, T: Encoder: Decoder](path: Path,
                                            initial: T): PureStorage[F, T] =
    CircePureStorage2(path, initial)

  def ensureSafe[F[_], T](locker: MLock[F])(
      pureStorage: PureStorage[F, T]): PureStorage[F, T] =
    new PureStorage[F, T] {
      override def modify(f: T => F[T]): F[T] =
        locker.greenLight(pureStorage.modify(f))

      override def read: F[T] = pureStorage.read

      override def zero: T = pureStorage.zero
    }

  import cats.implicits._
  def fromRef[F[_], T](ref: Ref[F, T], initial: T)(
      implicit F: Sync[F]): PureStorage[F, T] =
    new PureStorage[F, T] {
      override def modify(f: T => F[T]): F[T] =
        ref.get.flatMap(v => f(v)).flatMap(v => ref.set(v) *> F.pure(v))

      override def read: F[T] =
        ref.get

      override def zero: T = initial
    }

}

final case class CircePureStorage2[F[_], T: Encoder: Decoder](
    path: Path,
    zero: T)(implicit F: Sync[F])
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
  def modify(f: T => F[T]): F[T] =
    read.flatMap(f).flatTap(put)

  private def put(v: T): F[Unit] =
    F.delay(Files.writeString(path, v.asJson.spaces2)).void

  def read: F[T] =
    fetchFile.map(_.map(_.as[T].fold(throw _, identity)).getOrElse(zero))
}
