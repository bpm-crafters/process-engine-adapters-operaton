package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.*
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.operaton.bpm.engine.ExternalTaskService
import org.operaton.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

private val logger = KotlinLogging.logger {}

/**
 * This configuration configures the initial pull bound to the process application running phase.
 * It is not relying on any delivery strategies but just configures the initial pull to happen
 * and deliver tasks to the task handlers.
 *
 * It is an auto-configuration because Boot imports it directly from `AutoConfiguration.imports`;
 * ordering it after the adapter configuration guarantees that the shared adapter beans already
 * exist before the startup bindings are created.
 */
@AutoConfiguration
@AutoConfigureAfter(OperatonEmbeddedAdapterAutoConfiguration::class)
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
    lifecycle: ProcessApplicationLifecycle,
    taskService: TaskService,
    @Qualifier("operaton-embedded-process-definition-meta-data-resolver")
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
    subscriptionRepository: SubscriptionRepository,
    @Qualifier("operaton-embedded-user-task-worker-executor")
    executorService: ExecutorService,
    operatonEmbeddedAdapterProperties: OperatonEmbeddedAdapterProperties
  ) = OperatonEmbeddedInitialPullUserTasksDeliveryBinding(
    taskService = taskService,
    subscriptionRepository = subscriptionRepository,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    executorService = executorService,
    operatonEmbeddedAdapterProperties = operatonEmbeddedAdapterProperties
  ).also { binding ->
    lifecycle.registerParticipant(ProcessApplicationPhase.RUNNING - 10) { binding.pullUserTasks() }
  }

  @Bean("operaton-embedded-service-task-initial-pull")
  @Qualifier("operaton-embedded-service-task-initial-pull")
  @Conditional(OperatonEmbeddedAdapterServiceTaskInitialPullEnabledCondition::class)
  fun configureInitialPullForExternalServiceTaskDelivery(
    lifecycle: ProcessApplicationLifecycle,
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    operatonAdapterProperties: OperatonEmbeddedAdapterProperties,
    @Qualifier("operaton-embedded-service-task-worker-executor")
    executor: ThreadPoolExecutor,
    metrics: EmbeddedPullServiceTaskDeliveryMetrics
  ) = OperatonEmbeddedInitialPullServiceTasksDeliveryBinding(
    externalTaskService = externalTaskService,
    subscriptionRepository = subscriptionRepository,
    operatonAdapterProperties = operatonAdapterProperties,
    executor = executor,
    metrics = metrics
  ).also { binding ->
    lifecycle.registerParticipant(ProcessApplicationPhase.RUNNING - 20) { binding.pullServiceTasks() }
  }
}
