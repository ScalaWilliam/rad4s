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

import cats.effect.Sync

trait PureStorage[F[_], T] {
  def modify(f: Option[T] => F[T]): F[T]
  def modifyPure(f: Option[T] => T)(implicit F: Sync[F]): F[T] =
    modify(i => F.delay(f(i)))
  def read: F[Option[T]]
}
object PureStorage {}
