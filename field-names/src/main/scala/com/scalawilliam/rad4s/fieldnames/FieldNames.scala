package com.scalawilliam.rad4s.fieldnames

import shapeless._
import shapeless.labelled.FieldType

trait FieldNames[T] {
  def fieldNames: List[String]
}
object FieldNames {

  def fromList[T](names: List[String]): FieldNames[T] = new FieldNames[T] {
    override def fieldNames: List[String] = names
  }

  implicit def caseCaseClass[CaseClass, Representation <: HList](
      implicit labelledGeneric: LabelledGeneric.Aux[CaseClass, Representation],
      representationFieldNames: FieldNames[Representation])
    : FieldNames[CaseClass] = fromList(representationFieldNames.fieldNames)

  implicit def caseHNil: FieldNames[HNil] = fromList[HNil](Nil)

  implicit def caseHCons[FieldKey <: Symbol, FieldValueType, Rest <: HList](
      implicit
      keyWitness: Witness.Aux[FieldKey],
      restOfFieldNames: FieldNames[Rest])
    : FieldNames[FieldType[FieldKey, FieldValueType] :: Rest] =
    fromList(keyWitness.value.name :: restOfFieldNames.fieldNames)

}
