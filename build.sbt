name := "rad4s"
scalaVersion in ThisBuild := "2.13.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"
inThisBuild(List(
  organization := "com.scalawilliam",
  homepage := Some(url("https://github.com/ScalaWilliam/rad4s")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "ScalaWilliam",
      "ScalaWilliam",
      "hello@scalawilliam.com",
      url("https://www.scalawilliam.com")
    )
  )
))
skip in publish := true