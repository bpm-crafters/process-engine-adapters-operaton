package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

class OperatonRunTestContainer(tag: String) : GenericContainer<OperatonRunTestContainer>("camunda/camunda-bpm-platform:$tag") {

  init {
    withCommand("./camunda.sh", "--rest")
    withEnv("CAMUNDA_BPM_DEFAULT-SERIALIZATION-FORMAT", "application/json")
    withExposedPorts(8080)
    waitingFor(Wait
      .forHttp("/engine-rest/engine/")
      .forPort(8080)
    )
  }

}
