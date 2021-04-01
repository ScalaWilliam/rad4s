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

package com.scalawilliam.rad4s.chirps

import cats.effect.concurrent.{MVar, MVar2, Ref}
import cats.effect.implicits._
import cats.effect.{Concurrent, Sync}
import cats.implicits._
import io.circe.jawn.JawnParser
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import java.nio.file.{Files, Path}

object CircePureStorage {

  final class MLock[F[_]: Concurrent](mvar: MVar2[F, Unit]) {
    def acquire: F[Unit] =
      mvar.take

    def release: F[Unit] =
      mvar.put(())

    def greenLight[A](fa: F[A]): F[A] =
      acquire.bracket(_ => fa)(_ => release)
  }

  object MLock {
    // todo PR to cats docs
    def apply[F[_]: Concurrent]: F[MLock[F]] =
      MVar[F].of(()).map(ref => new MLock(ref))
  }

  def make[F[_]: Sync, T: Encoder: Decoder](path: Path): PureStorage[F, T] =
    CircePureStorage(path)

  def ensureSafe[F[_], T](locker: MLock[F])(
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
    F.delay(Files.write(path, v.asJson.spaces2.getBytes("UTF-8"))).void

  def read: F[Option[T]] =
    fetchFile.map(_.map(_.as[T].fold(throw _, identity)))
}
