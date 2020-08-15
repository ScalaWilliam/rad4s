name := "rad4s"
enablePlugins(GitVersioning)
import sbtrelease.Version

import ReleaseTransformations._
git.useGitDescribe in ThisBuild := true

releaseVersionBump := sbtrelease.Version.Bump.Minor
releaseProcess := Seq[ReleaseStep](
//  runClean,
  runTest,
  ReleaseStep { st =>
    val currentVersion = Version(Project.extract(st).get(version)).getOrElse(sys.error(s"Cannot extract version from '${Project.extract(st).get(version)}'"))
    val newVersion = currentVersion.withoutQualifier.bump(sbtrelease.Version.Bump.Minor).withoutQualifier.string
    st.log.info("Setting version to '%s'.".format(newVersion))
    reapply(Seq(version in ThisBuild := newVersion), st)
  },
  setNextVersion,
  tagRelease,
  publishArtifacts,
  pushChanges
)

scalaVersion in ThisBuild := "2.13.3"
libraryDependencies in ThisBuild += "org.scalatest" %% "scalatest" % "3.2.0" % "test"

inThisBuild(
  List(
    organization := "com.scalawilliam",
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
    name := "rad4s-mage",
    libraryDependencies += "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    libraryDependencies += "org.typelevel"  %% "cats-effect"   % "2.1.4" % Test,
    libraryDependencies += "org.typelevel"  %% "cats-core"     % "2.0.0" % Test,
    initialCommands in console := """import com.scalawilliam.mage._"""
  )

lazy val `field-names` = project
  .settings(
    name := "rad4s-field-names",
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
  )

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

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val `http4s-resource-servlet` = project
  .settings(
    name := "rad4s-http4s-resource-servlet",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-servlet"   % "0.21.4",
      "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
    )
  )
