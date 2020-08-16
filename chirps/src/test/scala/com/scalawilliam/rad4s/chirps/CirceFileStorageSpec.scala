package com.scalawilliam.rad4s.chirps

import java.nio.file.{Files, Path, Paths}

import cats.effect.{ContextShift, IO, Resource}
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import CirceFileStorageSpec._
import com.scalawilliam.rad4s.chirps.CircePureStorage.MLock

import scala.concurrent.ExecutionContext

object CirceFileStorageSpec {

  private implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  def makeRandomName: IO[Path] = IO.delay {
    Paths.get(s"${Math.abs(scala.util.Random.nextInt())}.map")
  }

  private val storageResource: Resource[IO, PureStorage[IO, Int]] = Resource
    .make(makeRandomName)(f => IO.delay(if (Files.exists(f)) Files.delete(f)))
    .flatMap { path =>
      Resource
        .liftF(MLock.apply[IO])
        .map(mlock =>
          CircePureStorage.ensureSafe(mlock)(
            CircePureStorage.make[IO, Int](path)))
    }

}

final class CirceFileStorageSpec extends AnyFunSuite {
  ioTest("It gets nothing for a basic read") {
    storageResource
      .use { storage =>
        storage.read
      }
      .map(oopt => assert(oopt.isEmpty))
  }
  ioTest("It gets something after modification") {
    storageResource
      .use { storage =>
        storage.modify(_ => IO.pure(2))
      }
      .map(oopt => assert(oopt == 2))
  }
  ioTest("It can read something after modification") {
    storageResource
      .use { storage =>
        storage.modify(_ => IO.pure(2)) *> storage.read
      }
      .map(oopt => assert(oopt.contains(2)))
  }
  ioTest("It gets read something after 2 modifications") {
    storageResource
      .use { storage =>
        val modifyTwice =
          storage.modify(maybeNum => IO.pure(maybeNum.fold(2)(n => n + 3)))
        modifyTwice *> modifyTwice *> storage.read
      }
      .map(oopt => assert(oopt.contains(5)))
  }
  def ioTest(name: String)(io: IO[Assertion]): Unit =
    test(name)(io.unsafeRunSync())

}
