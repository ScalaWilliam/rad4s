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

package com.scalawilliam.rad4s.http4s

import org.http4s.headers.`Content-Type`
import org.http4s.{Charset, DefaultCharset, EntityEncoder, MediaType}
import org.jsoup.nodes.Document

object JSoupDocumentEncoder extends JSoupDocumentEncoder

trait JSoupDocumentEncoder {
  implicit def jsoupDocumentEncoder[F[_]](
      implicit charset: Charset = DefaultCharset): EntityEncoder[F, Document] =
    contentEncoder(MediaType.text.html)

  private def contentEncoder[F[_]](mediaType: MediaType)(
      implicit charset: Charset): EntityEncoder[F, Document] =
    EntityEncoder
      .stringEncoder[F]
      .contramap[Document](content => content.outerHtml())
      .withContentType(`Content-Type`(mediaType, charset))
}
