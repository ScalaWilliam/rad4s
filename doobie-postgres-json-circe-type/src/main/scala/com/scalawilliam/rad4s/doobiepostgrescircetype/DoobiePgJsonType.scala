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
