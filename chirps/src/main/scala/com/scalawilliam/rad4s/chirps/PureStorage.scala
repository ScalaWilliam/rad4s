package com.scalawilliam.rad4s.chirps

import cats.effect.{IO, Sync}

trait PureStorage[F[_], T] {
  def modify(f: Option[T] => F[T]): F[T]
  def modifyPure(f: Option[T] => T)(implicit F: Sync[F]): F[T] =
    modify(i => F.delay(f(i)))
  def read: F[Option[T]]
}
object PureStorage {}
