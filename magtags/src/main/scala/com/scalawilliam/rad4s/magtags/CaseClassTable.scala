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

package com.scalawilliam.rad4s.magtags

import com.scalawilliam.rad4s.fieldnames.FieldNames
import magnolia._

import java.time.{LocalDate, YearMonth}
import scala.reflect.ClassTag

object CaseClassTable {

  import scalatags.Text.all._

  trait ShowHtml[T] {
    def render(t: T): Frag
  }

  object ShowHtml {
    object ImplicitToFrag {
      implicit def render[T: ShowHtml: FieldNames: ClassTag](
          stuff: List[T]): Frag =
        implicitly[ShowHtml[List[T]]].render(stuff)
    }
    def render[T: ShowHtml: FieldNames: ClassTag](stuff: List[T]): Frag =
      implicitly[ShowHtml[List[T]]].render(stuff)
    implicit def showList[T: ShowHtml: FieldNames](
        implicit classTag: ClassTag[T]): ShowHtml[List[T]] = { items =>
      table(
        `class` := s"sortable ${classTag.runtimeClass.getSimpleName.toLowerCase()}",
        thead(
          tr(
            implicitly[FieldNames[T]].fieldNames.map(k => th(k))
          )
        ),
        tbody(
          items.map(implicitly[ShowHtml[T]].render(_))
        )
      )
    }
    implicit val ShowString: ShowHtml[String]   = v => stringFrag(v)
    implicit val ShowInt: ShowHtml[Int]         = v => stringFrag(v.toString)
    implicit val ShowBoolean: ShowHtml[Boolean] = v => stringFrag(v.toString)
    implicit val ShowLong: ShowHtml[Long]       = v => stringFrag(v.toString)
    implicit val ShowBigDecimal: ShowHtml[BigDecimal] = v =>
      stringFrag(v.toString)
    implicit val ShowLocalDate: ShowHtml[LocalDate] = v =>
      stringFrag(v.toString)
    implicit val ShowYearMonth: ShowHtml[YearMonth] = v =>
      stringFrag(v.toString)
    implicit def ShowOptional[T](
        implicit showHtmlBasic: ShowHtml[T]): ShowHtml[Option[T]] =
      v => frag(v.map(showHtmlBasic.render))

  }

  object ShowHtmlDerivation {

    type Typeclass[T] = ShowHtml[T]

    implicit def gen[T]: ShowHtml[T] =
      macro Magnolia.gen[T]

    def dispatch[A](ctx: SealedTrait[ShowHtml, A]): ShowHtml[A] =
      (t: A) => {
        ctx.dispatch(t)(sub => sub.typeclass.render(sub.cast(t)))
      }
    def combine[T](ctx: CaseClass[ShowHtml, T]): ShowHtml[T] =
      (v: T) =>
        tr(
          ctx.parameters.map(vv => td(vv.typeclass.render(vv.dereference(v))))
      )
  }
}
