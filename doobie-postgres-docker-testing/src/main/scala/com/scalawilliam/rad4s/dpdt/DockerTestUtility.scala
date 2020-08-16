package com.scalawilliam.rad4s.dpdt

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.DockerCmdExecFactory
import com.github.dockerjava.core.{
  DefaultDockerClientConfig,
  DockerClientBuilder
}

final case class DockerTestUtility(dockerCmdExecFactory: DockerCmdExecFactory) {

  def withClient[T](f: DockerClient => T): T = {
    val client: DockerClient = DockerClientBuilder
      .getInstance(
        DefaultDockerClientConfig
          .createDefaultConfigBuilder()
          .build())
      .withDockerCmdExecFactory(dockerCmdExecFactory)
      .build()
    try f(client)
    finally client.close()
  }

}
