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

package com.scalawilliam.rad4s.doobiepostgrescircetype

import doobie._
import io.circe._
import io.circe.jawn._
import io.circe.syntax._
import org.postgresql.util.PGobject

object DoobiePgJsonType {

  implicit val jsonMeta: Meta[Json] = {
    import cats.syntax.all._
    import cats.data._
    Meta.Advanced
      .other[PGobject]("json")
      .timap[Json](a => parse(a.getValue).leftMap[Json](e => throw e).merge)(
        a => {
          val o = new PGobject
          o.setType("json")
          o.setValue(a.noSpaces)
          o
        }
      )
  }

  def jsonTypeGet[T: Decoder]: Get[T] =
    jsonMeta.get.map(json =>
      implicitly[Decoder[T]].decodeJson(json).fold(throw _, identity))

  def jsonTypePut[T: Encoder]: Put[T] =
    jsonMeta.put.contramap(j => implicitly[Encoder[T]].apply(j))

}
