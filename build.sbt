organization := "com.scalawilliam.rad4s"
name := "root"
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
scalaVersion in ThisBuild := "2.13.3"
libraryDependencies in ThisBuild += "org.scalatest" %% "scalatest" % "3.2.1" % "test"

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

lazy val mage = project
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    libraryDependencies += "org.typelevel"  %% "cats-effect"   % "2.1.4" % Test,
    libraryDependencies += "org.typelevel"  %% "cats-core"     % "2.1.1" % Test,
    initialCommands in console := """import com.scalawilliam.rad4s.mage._"""
  )

lazy val `field-names` = project
  .settings(
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
  )

lazy val `es1` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat"  %% "doobie-core"     % "0.9.0",
      "org.tpolecat"  %% "doobie-postgres" % "0.9.0",
      "org.typelevel" %% "cats-effect"     % "2.1.4"
    ),
    circe
  )

lazy val `chirps` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "2.1.4"
    ),
    circe
  )

lazy val magtags = project
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi"    %% "scalatags"    % "0.7.0",
      "com.propensive" %% "magnolia"     % "0.16.0",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )
  .dependsOn(`field-names`)

lazy val chirps2 = project
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "2.1.4"
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

lazy val `http4s-resource-servlet` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-servlet"   % "0.21.7",
      "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
    )
  )

lazy val `http4s-heroku-redirect` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl"  % "0.21.7",
      "org.http4s" %% "http4s-core" % "0.21.7"
    )
  )

lazy val `http4s-multipart-simple-form` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl"  % "0.21.7",
      "org.http4s" %% "http4s-core" % "0.21.7"
    )
  )

lazy val `doobie-postgres-json-circe-type` = project
  .settings(
    circe,
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core"     % "0.9.0",
      "org.tpolecat" %% "doobie-postgres" % "0.9.0"
    )
  )
