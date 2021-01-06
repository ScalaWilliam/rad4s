organization := "com.scalawilliam.rad4s"
name := "rad4s"
enablePlugins(GitVersioning)
import sbtrelease.Version

import ReleaseTransformations._
git.useGitDescribe in ThisBuild := true

releaseVersionBump := sbtrelease.Version.Bump.Minor
releaseProcess := Seq[ReleaseStep](
  runClean,
  runTest,
  ReleaseStep { st =>
    val currentVersion =
      Version(Project.extract(st).get(version)).getOrElse(sys.error(
        s"Cannot extract version from '${Project.extract(st).get(version)}'"))
    val newVersion = currentVersion.withoutQualifier
      .bump(sbtrelease.Version.Bump.Next)
      .withoutQualifier
      .string
    st.log.info("Setting version to '%s'.".format(newVersion))
    reapply(Seq(version in ThisBuild := newVersion), st)
  },
  tagRelease,
  publishArtifacts,
  pushChanges
)

bintrayOrganization := Some("scalawilliam")
scalaVersion in ThisBuild := "2.13.4"
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

val catsVersion = "2.3.0"
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
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
  )

val doobieVersion = "0.9.4"
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

scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
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

val http4sVersion = "0.21.13"
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

Global / onChangedBuildSource := IgnoreSourceChanges
