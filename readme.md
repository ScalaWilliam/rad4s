# rad4s

> High velocity Scala.
> A set of utilities to speed up things like table rendering, prototyping storage,
> generation of HTML tables and testing.

## `chirps`
Storage engine using JSON and plain files with a degree of concurrency support.

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "chirps" % "0.0.38"
```

## `chirps2`
Similar to `chirps`, but uses a default value.

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "chirps2" % "0.0.38"
```

## `doobie-postgres-docker-testing`
Enable reproducible testing with Doobie, PostgreSQL and Docker.
                                 
```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "doobie-postgres-docker-testing" % "0.0.42"
```

## `doobie-postgres-json-circe-type`
Directly embed a Circe-compatible type into Doobie queries for PostgreSQL.

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "doobie-postgres-json-circe-type" % "0.0.41"
```

## `es1`
Event-streamed storage engine for NDJson and PsotgreSQL.

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "es1" % "0.0.38"
```

## `field-names`

```scala
scala> final case class TestClass(a: String, b: Int)
scala> implicitly[com.scalawilliam.rad4s.fieldnames.FieldNames[TestClass]].fieldNames
List(a, b)
```

Based on shapeless - especially useful if you'd like to learn how to use shapeless.

Integratable with Kantan to provide headers automatically.

Install with:

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "field-names" % "0.0.38"
```

## `http4s-heroku-redirect`

Redirect to SSL by default for Heroku apps

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "http4s-heroku-redirect" % "0.0.38"
```


## `http4s-jsoup-encoder`
Support JSoup `Document` as a response body in http4s.
                                 
```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "http4s-jsoup-encoder" % "0.0.41"
```

## `http4s-multipart-simple-form`
Extract form fields (not files) for easy processing.

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "http4s-multipart-simple-form" % "0.0.41"
```

## `http4s-nested-routes`
Allow to nest routes within routes. Perfect for conditional routes
eg based on user level. This helps lift the security level higher
instead at the definition of endpoints.
                                 
```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "http4s-nested-routes" % "0.0.41"
```

## `http4s-resource-servlet`

Enabled you to start an HttpApp in http4s, parametrised by `ServletConfig`, to retrieve things like the context path (to create URLs).

Install with:

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "http4s-resource-servlet" % "0.0.38"
```

## `mage`

Reflection-free dynamic dispatch. Using Scala macros.

I use it to create new menu items and routes in my http4s applications
without having to create them explicitly. They typically have method signature like `IO[Response[IO]]`. 

```scala
scala> trait ExampleTrait {
  def callX: String = "X"
  def callY: String = "Y"
  def callZ: Int = 3
}

scala>  val tCalls = com.scalawilliam.rad4s.mage.Mage.mage[ExampleTrait, String]
val tCalls: scala.collection.immutable.Map[String,ExampleTrait => String] = Map(callX -> $Lambda$7225/0x00000001016ec840@5d92cfa6, callY -> $Lambda$7226/0x00000001016eb840@6efc083e)
```

Install with:
```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "mage" % "0.0.38"
```

## `magtags`
Render HTML tables with Magnolia and Scalatags

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "magtags" % "0.0.43"
```

