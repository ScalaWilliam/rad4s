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
- http4s-servlet enhancement

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

scala>  val tCalls = com.scalawilliam.mage.Mage.mage[ExampleTrait, String]
val tCalls: scala.collection.immutable.Map[String,ExampleTrait => String] = Map(callX -> $Lambda$7225/0x00000001016ec840@5d92cfa6, callY -> $Lambda$7226/0x00000001016eb840@6efc083e)
```
