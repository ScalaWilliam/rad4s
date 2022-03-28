ThisBuild / organization := "com.scalawilliam.rad4s"
name := "rad4s"

//ThisBuild / baseVersion := "0.60.0"

ThisBuild / version := "1.0.3-SNAPSHOT"
//ThisBuild / publishGithubUser := "ScalaWilliam"
//ThisBuild / publishFullName := "ScalaWilliam"

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value)
  Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//  Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test

val supportedScalaVersions = List("2.13.8", "3.0.1")

val has3 = crossScalaVersions := supportedScalaVersions

inThisBuild(
  List(
    organization := "com.scalawilliam.rad4s",
    homepage := Some(url("https://github.com/ScalaWilliam/rad4s")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "ScalaWilliam",
        "ScalaWilliam",
        "hello@scalawilliam.com",
        url("https://www.scalawilliam.com")
      )
    )
  ))
publish / skip := true

val catsVersion = "2.6.1"
val ceVersion   = "3.1.1"
lazy val mage = project
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    libraryDependencies += "org.typelevel"  %% "cats-effect"   % ceVersion % Test,
    libraryDependencies += "org.typelevel"  %% "cats-core"     % catsVersion % Test,
    console / initialCommands := """import com.scalawilliam.rad4s.mage._"""
  )

lazy val `field-names` = project
  .settings(
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.4",
    scalacOptions -= "-Xfatal-warnings"
//    fatalWarningsInCI := false
  )

val doobieVersion = "1.0.0-RC2"
lazy val `es1` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat"  %% "doobie-core"     % doobieVersion,
      "org.tpolecat"  %% "doobie-postgres" % doobieVersion,
      "org.typelevel" %% "cats-effect"     % ceVersion
    ),
    circe
  )

lazy val `chirps` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % ceVersion
    ),
    circe
  )

lazy val magtags = project
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi"    %% "scalatags"    % "0.9.4",
      "com.propensive" %% "magnolia"     % "0.17.0",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )
  .dependsOn(`field-names`)

lazy val chirps2 = project
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % ceVersion
    ),
    circe
  )
  .dependsOn(chirps)

def circeVersion = "0.14.1"

def circe =
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-Xfatal-warnings",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

val http4sVersion = "0.23.0"

lazy val `http4s-heroku-redirect` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl"  % http4sVersion,
      "org.http4s" %% "http4s-core" % http4sVersion
    )
  )

lazy val `http4s-multipart-simple-form` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl"  % http4sVersion,
      "org.http4s" %% "http4s-core" % http4sVersion
    )
  )

lazy val `http4s-nested-routes` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl"  % http4sVersion,
      "org.http4s" %% "http4s-core" % http4sVersion
    )
  )

lazy val `http4s-jsoup-encoder` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.jsoup"  % "jsoup"        % "1.14.1"
    )
  )

lazy val `doobie-postgres-json-circe-type` = project
  .settings(
    circe,
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion
    )
  )

lazy val `http4s-browsersync` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-core"    % http4sVersion,
      "org.typelevel" %% "log4cats-core"  % "2.1.1",
      "org.typelevel" %% "log4cats-slf4j" % "2.1.1"
    )
  )

Global / onChangedBuildSource := IgnoreSourceChanges

ThisBuild / versionScheme := Some("pvp")
