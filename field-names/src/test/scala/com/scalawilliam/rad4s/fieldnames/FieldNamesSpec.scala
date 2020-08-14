package com.scalawilliam.rad4s.fieldnames

import com.scalawilliam.rad4s.fieldnames.FieldNamesSpec.TestClass
import org.scalatest.freespec.AnyFreeSpec

object FieldNamesSpec {
  final case class TestClass(a: String, b: Int)
}

final class FieldNamesSpec extends AnyFreeSpec {
  "It works" in {
    assert(implicitly[FieldNames[TestClass]].fieldNames == List("a", "b"))
  }
}
