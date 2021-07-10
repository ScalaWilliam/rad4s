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

package com.scalawilliam.rad4s.chiprs2

import cats.effect.Sync
import com.scalawilliam.rad4s.chiprs2.CircePureStorage2.PureStorage
import io.circe.jawn.JawnParser
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import java.nio.file.{Files, Path}

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
    F.delay(Files.write(path, v.asJson.spaces2.getBytes("UTF-8"))).void

  def read: F[T] =
    fetchFile.map(_.map(_.as[T].fold(throw _, identity)).getOrElse(zero))
}
