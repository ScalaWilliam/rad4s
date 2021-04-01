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

package com.scalawilliam.rad4s.fieldnames

import shapeless._
import shapeless.labelled.FieldType

trait FieldNames[T] {
  def fieldNames: List[String]
}
object FieldNames {

  import shapeless._
  import shapeless.ops.record._
  import shapeless._
  import shapeless.ops.hlist.{Mapper, ToTraversable}

  import shapeless._
  import shapeless.ops.record._

  private object toName extends Poly1 {
    implicit def keyToName[A] = at[Symbol with A](_.name)
  }

  implicit def recordDecoder[A,
                             R <: HList,
                             LR <: HList,
                             K <: HList,
                             KL <: HList](
      implicit
      gen: Generic.Aux[A, R],
      lgen: LabelledGeneric.Aux[A, LR],
      kk: Keys.Aux[LR, K],
      m: Mapper.Aux[toName.type, K, KL],
      toSeq: ToTraversable.Aux[KL, List, String]): FieldNames[A] =
    new FieldNames[A] {
      def fieldNames: List[String] =
        kk.apply.map(toName).to[List]
    }

  def apply[T](implicit hasKeys: FieldNames[T]): FieldNames[T] =
    hasKeys

}
