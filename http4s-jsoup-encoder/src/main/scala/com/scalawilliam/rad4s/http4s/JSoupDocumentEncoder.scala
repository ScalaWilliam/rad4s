package com.scalawilliam.rad4s.http4s

import cats.Applicative
import org.http4s.{MediaType, EntityEncoder, Charset, DefaultCharset}
import org.http4s.headers.`Content-Type`
import org.jsoup.nodes.Document

object JSoupDocumentEncoder extends JSoupDocumentEncoder

trait JSoupDocumentEncoder {
  implicit def jsoupDocumentEncoder[F[_]: Applicative](
      implicit charset: Charset = DefaultCharset): EntityEncoder[F, Document] =
    contentEncoder(MediaType.text.html)

  private def contentEncoder[F[_]](mediaType: MediaType)(
      implicit charset: Charset): EntityEncoder[F, Document] =
    EntityEncoder
      .stringEncoder[F]
      .contramap[Document](content => content.outerHtml())
      .withContentType(`Content-Type`(mediaType, charset))
}
