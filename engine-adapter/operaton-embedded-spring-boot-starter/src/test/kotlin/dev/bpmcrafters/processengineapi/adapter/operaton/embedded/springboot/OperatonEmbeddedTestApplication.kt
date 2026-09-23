package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot

import com.tngtech.jgiven.integration.spring.EnableJGiven
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties.ExternalServiceTaskDeliveryStrategy.EMBEDDED_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.decision.EvaluateDecisionApi
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.operaton.bpm.engine.ProcessEngine
import org.operaton.bpm.engine.RuntimeService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


@EnableJGiven
@SpringBootApplication
class OperatonEmbeddedTestApplication {

  @ConditionalOnServiceTaskDeliveryStrategy(
    strategy = EMBEDDED_SCHEDULED
  )
  @Bean
  fun processTestHelper(
    processEngine: ProcessEngine,
    runtimeService: RuntimeService,
    startProcessApi: StartProcessApi,
    taskSubscriptionApi: TaskSubscriptionApi,
    userTaskDelivery: EmbeddedPullUserTaskDelivery,
    externalTaskDelivery: EmbeddedPullServiceTaskDelivery,
    userTaskCompletionApi: UserTaskCompletionApi,
    serviceTaskCompletionApi: ServiceTaskCompletionApi,
    subscriptionRepository: SubscriptionRepository,
    evaluateDecisionApi: EvaluateDecisionApi,
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
  ): ProcessTestHelper = OperatonEmbeddedSpringProcessTestHelper(
    runtimeService = runtimeService,
    startProcessApi = startProcessApi,
    taskSubscriptionApi = taskSubscriptionApi,
    userTaskDelivery = userTaskDelivery,
    externalTaskDelivery = externalTaskDelivery,
    userTaskCompletionApi = userTaskCompletionApi,
    serviceTaskCompletionApi = serviceTaskCompletionApi,
    subscriptionRepository = subscriptionRepository,
    evaluateDecisionApi = evaluateDecisionApi,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
  )

}

