package com.scalawilliam.rad4s.dpdt

import java.util.concurrent.TimeUnit

import cats.effect.{ContextShift, IO}
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.{Frame, Ports}
import com.github.dockerjava.api.model.Ports.Binding
import com.github.dockerjava.core.command.LogContainerResultCallback
import com.scalawilliam.rad4s.dpdt.PostgresTestUtility.DefaultOptions
import doobie.util.transactor.Transactor
import scala.jdk.CollectionConverters._

/** This code is not particularly refined! TODO rewrite in pure FP. */
object PostgresTestUtility {
  // need to do beforehand:
  // $ docker pull postgres:10.6

  final case class DefaultOptions(image: String = "postgres:10.6",
                                  username: String = "pg",
                                  dbName: String = "pg",
                                  containerName: String = "test-postgres",
                                  password: String = "pg",
                                  defaultPort: Int = 5432,
                                  forceIpAddress: Option[String] = None)
}

final case class PostgresTestUtility(dockerClient: DockerClient) {

  def withTransactor[T](f: Transactor[IO] => T)(
      implicit cs: ContextShift[IO],
      defaultOptions: DefaultOptions): T = {
    import defaultOptions._
    import com.github.dockerjava.api.model.ExposedPort

    val tcp5432  = ExposedPort.tcp(defaultPort)
    val boundPot = Binding.bindPort(defaultPort)

    val portBindings = new Ports()
    portBindings.bind(tcp5432, boundPot)

    def createContainer() =
      dockerClient
        .createContainerCmd(image)
        .withName(containerName)
        .withEnv(s"POSTGRES_USER=${username}",
                 s"POSTGRES_DB=${dbName}",
                 s"POSTGRES_PASSWORD=${password}")
        .withExposedPorts(tcp5432)
        .withPortBindings(portBindings)
        .exec()

    def fetchExistingContainers()
      : List[_root_.com.github.dockerjava.api.model.Container] =
      dockerClient
        .listContainersCmd()
        .withShowAll(true)
        .exec()
        .asScala
        .toList
        .filter(_.getNames.contains("/" + containerName))

    val existingContainers = fetchExistingContainers()
    if (existingContainers.nonEmpty) {
      existingContainers.foreach(container =>
        dockerClient.removeContainerCmd(container.getId).withForce(true).exec())

      while (fetchExistingContainers().nonEmpty) {
        println("Still exists, waiting...")
        Thread.sleep(1000)
      }
      println("Okie, we can go ahead!")
    }

    val container = try { createContainer() } catch {
      case e: org.newsclub.net.unix.AFUNIXSocketException =>
        createContainer()
    }

    // there are potential warnings here
    //      Option(container.getWarnings).map(_.toList.toString).foreach(s => info(s))
    //      info(container.getId)
    try {
      dockerClient.startContainerCmd(container.getId).exec()
      var lines = List.empty[String]
      val callback: LogContainerResultCallback =
        new LogContainerResultCallback {
          override def onNext(item: Frame): Unit = {
            lines ::= item.toString
            super.onNext(item)
          }
        }

      def isReady: Boolean =
        lines.exists(
          _.contains("database system is ready to accept connections"))

      def requestData() =
        dockerClient
          .logContainerCmd(container.getId)
          .withStdOut(true)
          .withStdErr(true)
          .withTimestamps(true)
          .withTail(10)
          .exec(callback)
          .awaitCompletion(1, TimeUnit.SECONDS)

      // todo more elegantly...
      while (!isReady) requestData()

      import cats.effect._

      val inspection =
        dockerClient.inspectContainerCmd(container.getId).exec()
      // technically we are supposed to get the ip via bridge, but it seems
      // we have to connect direct to our host instead... Windows quirk?

      val inspectionIpAddress =
        inspection.getNetworkSettings.getNetworks.get("bridge").getIpAddress

      val ipAddress = forceIpAddress.getOrElse(inspectionIpAddress)

      val url =
        s"jdbc:postgresql://${ipAddress}:${boundPot.getHostPortSpec}/${dbName}?user=${username}&password=${password}"

      val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
        driver = "org.postgresql.Driver",
        url = url
      )

      f(transactor)
    } finally {
      dockerClient.stopContainerCmd(container.getId).exec()
      dockerClient.removeContainerCmd(container.getId).exec()
    }
  }

}
