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
