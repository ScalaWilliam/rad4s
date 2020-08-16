package com.scalawilliam.rad4s.http4s

import cats.Monad
import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import org.http4s.EntityDecoder
import org.http4s.multipart.Multipart

final case class MultipartSimpleForm(items: Map[String, Vector[String]])

object MultipartSimpleForm {

  implicit def multipart[F[_]: Sync](
      implicit ed: EntityDecoder[F, Multipart[F]],
      F: Monad[F]): EntityDecoder[F, MultipartSimpleForm] = {
    ed.map { multipartData =>
      import fs2._
      import fs2.text._
      val toDecode = multipartData.parts
        .filter(
          part =>
            part.headers
              .get(org.http4s.headers.`Content-Disposition`)
              .exists(disposition =>
                disposition.dispositionType == "form-data"
                  && !disposition.parameters.contains("filename"))
        )
        .flatMap { part =>
          part.name.map { partName =>
            part.body.through(utf8Decode).map { decodedString =>
              partName -> decodedString
            }
          }
        }

      def cvt[T](i: Vector[Stream[F, T]]): F[Vector[T]] =
        Stream(i: _*).covary[F].flatten.compile.toVector
      cvt(toDecode)
        .map(_.groupBy(_._1).view.mapValues(_.map(_._2)).toMap)
        .map(MultipartSimpleForm.apply)
    }
  }.flatMapR(fr => EitherT(fr.map(Right.apply)))
}
