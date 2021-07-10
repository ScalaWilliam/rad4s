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

package com.scalawilliam.rad4s.http4s.herokuredirect
import com.scalawilliam.rad4s.http4s.herokuredirect.HerokuHttpsRedirect._
import org.http4s.headers.Host
import org.scalatest.freespec.AnyFreeSpec
import org.typelevel.ci.CIString

final class HerokuHttpsRedirectSpec extends AnyFreeSpec {
  import org.http4s._

  "It fails for default case" in {
    assert(!isSecure(Request()))
  }
  "It fails for secure case" in {
    assert(isSecure(Request(
      headers = Headers.empty.put(Header.Raw(CIString(HeaderName), WhenSsl)))))
  }
  "Host header can be turned into a URI" in {
    assert(hostToUri(Host("test.com")).renderString == "https://test.com")
  }
}
