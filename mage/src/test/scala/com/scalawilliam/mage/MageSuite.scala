package com.scalawilliam.mage

import org.scalatest.funsuite.AnyFunSuite
import MageSuite._
import cats.effect.IO

object MageSuite {

  trait SampleTrait {
    def y1: String

    def y2: String

    def yInvalid(x: String): String

    def y3: Int

    def gen: List[String]

    def gen2: List[Int]

    def gen4: Either[String, SampleTrait]

    def otherCC: CC
  }

  final case class CC()

  def mageMe: Map[String, SampleTrait => String] =
    Mage.mage[SampleTrait, String]

  def mageListString: Map[String, SampleTrait => List[String]] =
    Mage.mage[SampleTrait, List[String]]

  final case class SampleClass(x: String, y: Int) {
    def z: IO[String] = IO.pure(s"Z-${x}")
  }

  def sampleClassMage: Map[String, SampleClass => IO[String]] =
    Mage.mage[SampleClass, IO[String]]

  def moreComplex =
    Mage.mage[SampleTrait, Either[String, SampleTrait]]

  def forCC =
    Mage.mage[SampleTrait, CC]
}

final class MageSuite extends AnyFunSuite {
  test("Mage works") {
    assert(mageMe.keySet == Set("y1", "y2"))
  }
  val sampleImpl: SampleTrait = new SampleTrait {
    override def y1: String = "Y1"

    override def y2: String = "Y2"

    override def y3: Int = 3

    override def yInvalid(x: String): String = x

    override def gen: List[String] = List("GEN")

    override def gen2: List[Int] = List(2)

    override def gen4: Either[String, SampleTrait] =
      Left("ME")

    override def otherCC: CC = ???
  }
  test("Works for CC") {
    assert(forCC.contains("otherCC"))
  }

  test("gen4 ok too") {
    assert(moreComplex.contains("gen4"))
  }
  test("Mage does exscute") {
    assert(mageMe("y1")(sampleImpl) == "Y1")
  }

  def appliedMage: Map[String, String] =
    mageMe.view.mapValues(_(sampleImpl)).toMap

  test("Second mage works") {
    assert(appliedMage("y1") == "Y1")
  }

  test("List[String] only has 1 member") {
    assert(mageListString.size == 1)
  }
  test("Works for lis[string]") {
    assert(mageListString("gen")(sampleImpl) == List("GEN"))
  }

  test(
    "It works on a case class, but case classes have some more methods that have String") {
    assert(sampleClassMage.keySet == Set("z"))
  }
  test("It works on a case class value") {
    assert(
      sampleClassMage("z")(SampleClass(x = "X", y = 1))
        .unsafeRunSync() == "Z-X")
  }
}
