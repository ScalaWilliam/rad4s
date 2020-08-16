# rad4s

> High velocity Scala

- Generic HTML table generator
- Generic data adapter with different backends
    - In-memory based
    - JSON file based
    - PostgreSQL based
- Event streaming adapter with different backends
    - NDJSON based
    - PostgreSQL/JSON based
- Direct file streaming adapter
    - TSV-based
- Method-to-map-of-functions adapter for rapid development (`mage`)
- http4s-servlet enhancement (`http4s-resource-servlet`)
- shapeless-based type-class to get case class field names automatically (`field-names`)

## `mage`

Reflection-free dynamic dispatch.
I use it to create new menu items and routes in my http4s applications
without having to create them explicitly. They typically have a method signature
`IO[Response[IO]]`. 

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
libraryDependencies += "com.scalawilliam.rad4s" %% "mage" % "0.0.29"
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
libraryDependencies += "com.scalawilliam.rad4s" %% "field-names" % "0.0.29"
```

## `http4s-resource-servlet`

Enabled you to start an HttpApp in http4s, parametrised by `ServletConfig`, to retrieve things like the context path (to create URLs).

Install with:

```scala
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
libraryDependencies += "com.scalawilliam.rad4s" %% "http4s-resource-servlet" % "0.0.29"
```

## `http4s-heroku-redirect`

Redirect to SSL by default for Heroku apps

## `chirps`
Storage engine using JSON and plain files with a degree of concurrency support.

## `chirps2`
Similar to `chirps`, but uses a default value.
