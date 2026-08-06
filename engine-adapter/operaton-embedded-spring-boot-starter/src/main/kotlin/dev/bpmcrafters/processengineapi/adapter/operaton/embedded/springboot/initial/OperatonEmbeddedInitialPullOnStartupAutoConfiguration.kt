package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.*
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

private val logger = KotlinLogging.logger {}

/**
 * This configuration configures the initial pull bound to the application started event.
 * It is not relying on any delivery strategies but just configures the initial pull to happen
 * and deliver tasks to the task handlers.
 *
 * It is an auto-configuration because Boot imports it directly from `AutoConfiguration.imports`;
 * ordering it after the adapter configuration guarantees that the shared adapter beans already
 * exist before the startup bindings are created.
 */
@AutoConfiguration
@AutoConfigureAfter(OperatonEmbeddedAdapterAutoConfiguration::class)
@EnableAsync
@Conditional(OperatonEmbeddedAdapterEnabledCondition::class)
class OperatonEmbeddedInitialPullOnStartupAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-OPERATON-EMBEDDED-203: Configuration for initial pull applied." }
  }

  @Bean("operaton-embedded-user-task-initial-pull")
  @Qualifier("operaton-embedded-user-task-initial-pull")
  @Conditional(OperatonEmbeddedAdapterUserTaskInitialPullEnabledCondition::class)
  fun configureInitialPullForUserTaskDelivery(
    taskService: TaskService,
    @Qualifier("operaton-embedded-process-definition-meta-data-resolver")
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
    subscriptionRepository: SubscriptionRepository,
    @Qualifier("operaton-embedded-user-task-worker-executor")
    executorService: ExecutorService
  ) = OperatonEmbeddedInitialPullUserTasksDeliveryBinding(
    taskService = taskService,
    subscriptionRepository = subscriptionRepository,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    executorService = executorService
  )

  @Bean("operaton-embedded-service-task-initial-pull")
  @Qualifier("operaton-embedded-service-task-initial-pull")
  @Conditional(OperatonEmbeddedAdapterServiceTaskInitialPullEnabledCondition::class)
  fun configureInitialPullForExternalServiceTaskDelivery(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    adapterProperties: OperatonEmbeddedAdapterProperties,
    @Qualifier("operaton-embedded-service-task-worker-executor")
    executor: ThreadPoolExecutor,
    metrics: EmbeddedPullServiceTaskDeliveryMetrics
  ) = OperatonEmbeddedInitialPullServiceTasksDeliveryBinding(
    externalTaskService = externalTaskService,
    subscriptionRepository = subscriptionRepository,
    adapterProperties = adapterProperties,
    executor = executor,
    metrics = metrics
  )
}
