package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi
import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestInitializationRequest
import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestInitializer
import org.camunda.community.rest.client.FeignClientConfiguration
import org.camunda.community.rest.variables.ValueMapperConfiguration
import org.springframework.beans.factory.getBean
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

private const val ADAPTER_PROPERTIES_PREFIX = "dev.bpm-crafters.process-api.adapter.operaton-remote"

class OperatonRemoteProcessTestInitializer : ProcessTestInitializer {

  private var container: GenericContainer<*>? = null

  companion object {
    const val QUALIFIER = "operaton-remote"
  }

  override fun qualifier() = QUALIFIER

  override fun initialize(request: ProcessTestInitializationRequest): OperatonRemoteProcessTestContext {
    val engineConfiguration: OperatonRemoteProcessTestEngine? =
      request.testClass.getAnnotation(OperatonRemoteProcessTestEngine::class.java)
    val configuredUrl: String? = engineConfiguration?.url?.takeIf { it.isNotBlank() }

    val url = if (configuredUrl == null) {
      this.container = startContainer(engineConfiguration?.image)
      "http://${container?.host}:${container?.firstMappedPort}/engine-rest"
    } else {
      configuredUrl
    }

    val workerId = "process-test-${request.testClass.simpleName}"
    val application = startApplication(url, workerId, request.configuration)

    return OperatonRemoteProcessTestContext(
      application = application,
      stopHandler = { this.container?.stop() },
      runtimeApis = OperatonRemoteProcessTestRuntimeApis(
        startProcessApi = application.getBean<StartProcessApi>(),
        taskSubscriptionApi = application.getBean<TaskSubscriptionApi>(),
        userTaskCompletionApi = application.getBean<UserTaskCompletionApi>(),
        userTaskModificationApi = application.getBean<UserTaskModificationApi>(),
        serviceTaskCompletionApi = application.getBean<ServiceTaskCompletionApi>(),
        correlationApi = application.getBean<CorrelationApi>(),
        signalApi = application.getBean<SignalApi>(),
        deploymentApi = application.getBean<DeploymentApi>(),
      ),
      workerId = workerId,
    )
  }

  private fun startContainer(image: String?): GenericContainer<*> =
    GenericContainer(image ?: OperatonRemoteProcessTestEngine.DEFAULT_IMAGE)
      .withEnv("OPERATON_BPM_DEFAULT-SERIALIZATION-FORMAT", "application/json")
      .withExposedPorts(8080)
      .waitingFor(Wait.forHttp("/engine-rest/engine/").forPort(8080))
      .apply { start() }

  private fun startApplication(
    url: String,
    workerId: String,
    initializerConfiguration: Map<String, String>,
  ): ConfigurableApplicationContext =
    SpringApplicationBuilder(OperatonRemoteProcessTestApplication::class.java)
      .web(WebApplicationType.NONE)
      .properties(
        mapOf(
          "feign.client.config.default.url" to url,
          "$ADAPTER_PROPERTIES_PREFIX.enabled" to "true",
          "$ADAPTER_PROPERTIES_PREFIX.service-tasks.worker-id" to workerId,
          "$ADAPTER_PROPERTIES_PREFIX.service-tasks.delivery-strategy" to "REMOTE_SCHEDULED",
          "$ADAPTER_PROPERTIES_PREFIX.user-tasks.delivery-strategy" to "REMOTE_SCHEDULED",
          "spring.task.scheduling.enabled" to "false",
        ) + initializerConfiguration
      )
      .run()
}

@SpringBootApplication
@ImportAutoConfiguration(FeignClientConfiguration::class, ValueMapperConfiguration::class)
private class OperatonRemoteProcessTestApplication {

  @Bean
  fun objectMapper(): ObjectMapper = ObjectMapper()
    .registerModule(KotlinModule.Builder().build())
    .registerModule(Jdk8Module())
    .registerModule(JavaTimeModule())
}
