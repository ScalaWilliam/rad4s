ThisBuild / organization := "com.scalawilliam.rad4s"
name := "rad4s"

ThisBuild / baseVersion := "0.60.0"

ThisBuild / publishGithubUser := "ScalaWilliam"
ThisBuild / publishFullName := "ScalaWilliam"

scalaVersion in ThisBuild := "2.13.5"
libraryDependencies in ThisBuild += "org.scalatest" %% "scalatest" % "3.2.3" % "test"

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
skip in publish := true

val catsVersion = "2.3.1"
lazy val mage = project
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    libraryDependencies += "org.typelevel"  %% "cats-effect"   % catsVersion % Test,
    libraryDependencies += "org.typelevel"  %% "cats-core"     % catsVersion % Test,
    initialCommands in console := """import com.scalawilliam.rad4s.mage._"""
  )

lazy val `field-names` = project
  .settings(
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3",
    scalacOptions -= "-Xfatal-warnings",
    fatalWarningsInCI := false
  )

val doobieVersion = "0.10.0"
lazy val `es1` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat"  %% "doobie-core"     % doobieVersion,
      "org.tpolecat"  %% "doobie-postgres" % doobieVersion,
      "org.typelevel" %% "cats-effect"     % catsVersion
    ),
    circe
  )

lazy val `chirps` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsVersion
    ),
    circe
  )

lazy val magtags = project
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi"    %% "scalatags"    % "0.9.2",
      "com.propensive" %% "magnolia"     % "0.17.0",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )
  .dependsOn(`field-names`)

lazy val chirps2 = project
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsVersion
    ),
    circe
  )
  .dependsOn(chirps)

def circeVersion = "0.13.0"

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

val http4sVersion = "0.21.15"
lazy val `http4s-resource-servlet` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-servlet"   % http4sVersion,
      "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
    )
  )

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
      "org.jsoup"  % "jsoup"        % "1.13.1"
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

lazy val `fs2-letsencrypt` = project
  .settings(
    libraryDependencies += "co.fs2"           %% "fs2-io"      % "3.0.1",
    libraryDependencies += "org.bouncycastle" % "bcprov-jdk16" % "1.46"
  )

Global / onChangedBuildSource := IgnoreSourceChanges

enablePlugins(SonatypeCiReleasePlugin)
ThisBuild / spiewakCiReleaseSnapshots := true
ThisBuild / spiewakMainBranches := Seq("master")
