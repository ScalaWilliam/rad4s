package com.scalawilliam.rad4s.http4s.herokuredirect
import HerokuHttpsRedirect._
import org.http4s.headers.Host
import org.scalatest.freespec.AnyFreeSpec

final class HerokuHttpsRedirectSpec extends AnyFreeSpec {
  import org.http4s._

  "It fails for default case" in {
    assert(!isSecure(Request()))
  }
  "It fails for secure case" in {
    assert(
      isSecure(
        Request(headers = Headers.empty.put(Header(HeaderName, WhenSsl)))))
  }
  "Host header can be turned into a URI" in {
    assert(hostToUri(Host("test.com")).renderString == "https://test.com")
  }
}
