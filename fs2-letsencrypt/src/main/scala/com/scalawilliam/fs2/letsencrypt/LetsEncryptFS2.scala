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

package com.scalawilliam.fs2.letsencrypt

import cats.effect.kernel.Async
import cats.effect.{Resource, Sync}
import cats.implicits._
import com.scalawilliam.fs2.letsencrypt.LetsEncryptFS2.{
  CertificateAlias,
  PrivateKeyAlias,
  randomPassword
}
import fs2.io.net.tls.TLSContext
import org.bouncycastle.util.io.pem.PemReader

import java.io.{ByteArrayInputStream, FileReader, Reader}
import java.nio.file.{Path, Paths}
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.{KeyException, KeyFactory, KeyStore, PrivateKey}
import javax.net.ssl.{KeyManagerFactory, SSLContext}

object LetsEncryptFS2 {

  def fromLetsEncryptDirectory[F[_]: Sync](
      certificateDirectory: Path): Resource[F, LetsEncryptFS2] =
    (loadChain(certificateDirectory), loadPrivateKey(certificateDirectory))
      .mapN(new LetsEncryptFS2(_, _))

  val FullChainPemFile      = "fullchain.pem"
  val PrivateKeyPemFilename = "privkey.pem"

  def fromEnvironment[F[_]: Sync]: Resource[F, LetsEncryptFS2] =
    Resource
      .eval {
        Sync[F]
          .delay {
            Paths
              .get(
                sys.env
                  .get(EnvVarName)
                  .orElse(sys.props.get(SysPropertyName))
                  .getOrElse(
                    sys.error(
                      s"Expected environment variable '$EnvVarName' or system property '$SysPropertyName'"
                    )
                  )
              )
              .toAbsolutePath
          }
      }
      .flatMap(path => fromLetsEncryptDirectory(path))

  val EnvVarName      = "LETSENCRYPT_CERT_DIR"
  val SysPropertyName = "letsencrypt.cert.dir"

  private[letsencrypt] val PrivateKeyAlias  = "PrivateKeyAlias"
  private[letsencrypt] val CertificateAlias = "CertificateAlias"

  private[letsencrypt] def extractDER(reader: => Reader): List[Array[Byte]] = {
    val readerInstance = reader
    try {
      val pemReader = new PemReader(readerInstance)
      try Iterator
        .continually(Option(pemReader.readPemObject()))
        .takeWhile(_.isDefined)
        .flatten
        .flatMap(o => Option(o.getContent))
        .toList
      finally pemReader.close()
    } finally readerInstance.close()
  }

  private def loadPrivateKey[F[_]: Sync](
      certificateDirectory: Path): Resource[F, Array[Byte]] =
    clearableByteArray[F] {
      Sync[F].delay {
        val privateKeyPath =
          certificateDirectory.resolve(PrivateKeyPemFilename).toAbsolutePath
        extractDER(new FileReader(privateKeyPath.toFile)).headOption
          .getOrElse(
            throw new KeyException(
              s"Could not extract a private key from ${privateKeyPath}"
            )
          )
      }
    }

  private def loadChain[F[_]: Sync](
      certificateDirectory: Path): Resource[F, List[Array[Byte]]] =
    clearableListByteArray {
      Sync[F].delay {
        val chainPath =
          certificateDirectory.resolve(FullChainPemFile).toAbsolutePath
        extractDER(new FileReader(chainPath.toFile))
          .ensuring(_.nonEmpty,
                    s"Could not extract a single certificate from $chainPath")
      }
    }

  private def clearableCharArray[F[_]: Sync](
      f: F[Array[Char]]): Resource[F, Array[Char]] =
    Resource.make(f)(array =>
      Sync[F].delay {
        java.util.Arrays.fill(array, '0')
    })

  private def clearableByteArray[F[_]: Sync](
      f: F[Array[Byte]]): Resource[F, Array[Byte]] =
    Resource.make(f)(array =>
      Sync[F].delay {
        java.util.Arrays.fill(array, 0.toByte)
    })

  private def clearableListByteArray[F[_]: Sync](
      f: F[List[Array[Byte]]]): Resource[F, List[Array[Byte]]] =
    Resource.make(f)(list =>
      Sync[F].delay {
        list.foreach(array => java.util.Arrays.fill(array, 0.toByte))
    })

  private val PrintableRange = 0x20 to 0x7E

  private[letsencrypt] def randomPassword[F[_]: Sync]
    : Resource[F, Array[Char]] =
    Resource.eval(Sync[F].delay(16 + scala.util.Random.nextInt(20))).flatMap {
      length =>
        clearableCharArray[F] {
          Sync[F].delay {
            Array.fill(length) {
              PrintableRange(
                scala.util.Random.nextInt(PrintableRange.length - 1)).toChar
            }
          }
        }
    }

}

final class LetsEncryptFS2(certificateChain: List[Array[Byte]],
                           privateKeyBytes: Array[Byte]) {

  def addToKeyStore[F[_]: Sync](keyStore: KeyStore,
                                withPassword: Array[Char]): F[Unit] =
    Sync[F].delay {
      val certificates = certificateChain.map { bytes =>
        CertificateFactory
          .getInstance("X.509")
          .generateCertificate(new ByteArrayInputStream(bytes))
      }
      certificates.zipWithIndex.foreach {
        case (certificate, index) =>
          keyStore.setCertificateEntry(s"$CertificateAlias$index", certificate)
      }
      keyStore.setKeyEntry(
        PrivateKeyAlias,
        makePrivateKey(),
        withPassword,
        certificates.toArray
      )
    }

  private[letsencrypt] def makePrivateKey(): PrivateKey =
    KeyFactory
      .getInstance("RSA")
      .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes))

  def sslContextResource[F[_]: Sync]: Resource[F, SSLContext] =
    for {
      internalPassword <- randomPassword[F]
      keyStore <- Resource.eval {
        Sync[F].delay {
          val keyStore = KeyStore.getInstance("PKCS12")
          keyStore.load(null)
          keyStore
        }
      }
      _ <- Resource.eval(addToKeyStore(keyStore, internalPassword))
      sslContext <- Resource.eval {
        Sync[F].delay {
          val keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
          keyManagerFactory.init(keyStore, internalPassword)
          val sslContext = SSLContext.getInstance("TLS")
          sslContext.init(keyManagerFactory.getKeyManagers, null, null)
          sslContext
        }
      }
    } yield sslContext

  def tlsContextResource[F[_]: Async]: Resource[F, TLSContext[F]] =
    sslContextResource[F].map(
      sslContext =>
        TLSContext.Builder
          .forAsync[F]
          .fromSSLContext(sslContext))

}
