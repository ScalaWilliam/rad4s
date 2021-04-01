/*
 * Copyright 2021 ScalaWilliam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scalawilliam.rad4s.es1

import java.nio.file.{Files, Path, Paths}
import java.time.Instant

import cats.effect.{IO, Resource}
import com.scalawilliam.rad4s.es1.EventStore.Event
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite

final class EventStoreSpec extends AnyFunSuite {

  def tempFile: Resource[IO, Path] =
    Resource.make(IO.delay(Files.createTempFile("test", "test")))(f =>
      IO.delay(Files.delete(f)))

  def testEvent: IO[Event] =
    IO.delay { Instant.now() }.map { instant =>
      Event(eventTime = instant,
            eventName = "test-event",
            eventData = "test-data")
    }

  testOn("ndjson")(tempFile.flatMap(path =>
    Resource.pure[IO, EventStore[IO]](EventStore.ndJsonFileStore[IO](path))))

  testOn("json")(tempFile.flatMap(path =>
    Resource.pure[IO, EventStore[IO]](EventStore.jsonFileStore[IO](path))))

  def testIO[T](name: String)(ctor: IO[Assertion]): Unit =
    test(name)(ctor.unsafeRunSync())

  import cats._
  import cats.implicits._
  def testOn(name: String)(ctor: Resource[IO, EventStore[IO]]): Unit = {

    testIO(s"$name: file store works for a temp file and shows no data") {
      ctor.use(_.listEvents).map(e => assert(e.isEmpty))
    }

    testIO(s"$name: file store adds a record and shows data") {
      ctor.use(es =>
        testEvent.flatMap(evt =>
          es.putEvent(evt) *> es.listEvents.map(evts =>
            assert(evts == List(evt)))))
    }

    test(s"$name: file store adds two records and shows two datas") {
      ctor.use(es =>
        testEvent.flatMap(evt =>
          es.putEvent(evt) *> es.putEvent(evt) *> es.listEvents.map(evts =>
            assert(evts.size == 2))))
    }

    test(s"$name: the second type is second") {
      ctor.use(
        es =>
          testEvent.flatMap(
            evt =>
              es.putEvent(evt) *> es.putEvent(
                evt.copy(eventName = "second-type")) *> es.listEvents.map(
                evts => assert(evts.last.eventName == "second-type"))))
    }
  }

}
