package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterEnabledCondition
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.registerParticipant
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.client.OfficialClientServiceTaskAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient
import org.camunda.community.rest.client.api.TaskApiClient
import org.camunda.community.rest.variables.ValueMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

private val logger = KotlinLogging.logger {}

/**
 * This configuration configures the initial pull bound to the process application running phase.
 * It is not relying on any delivery strategies but just configures the initial pull to happen
 * and deliver tasks to the task handlers.
 */
@AutoConfiguration
@AutoConfigureAfter(OfficialClientServiceTaskAutoConfiguration::class)
@Conditional(OperatonRemoteAdapterEnabledCondition::class)
class OperatonRemoteInitialPullOnStartupAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-OPERATON-REMOTE-203: Configuration applied." }
  }

  @Bean("operaton-remote-user-task-initial-pull")
  @Qualifier("operaton-remote-user-task-initial-pull")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.execute-initial-pull-on-startup"])
  fun configureInitialPullForUserTaskDelivery(
    lifecycle: ProcessApplicationLifecycle,
    taskApiClient: TaskApiClient,
    processDefinitionApiClient: ProcessDefinitionApiClient,
    subscriptionRepository: SubscriptionRepository,
    @Qualifier("operaton-remote-user-task-worker-executor")
    executorService: ExecutorService,
    valueMapper: ValueMapper,
    @Qualifier("operaton-remote-process-definition-meta-data-resolver")
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
    operatonAdapterProperties: OperatonRemoteAdapterProperties
  ) = OperatonRemoteInitialPullUserTasksDeliveryBinding(
    taskApiClient = taskApiClient,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    subscriptionRepository = subscriptionRepository,
    executorService = executorService,
    valueMapper = valueMapper,
    operatonRemoteAdapterProperties = operatonAdapterProperties
  ).also { binding ->
    lifecycle.registerParticipant(ProcessApplicationPhase.RUNNING - 10) { binding.pullUserTasks() }
  }

  @Bean("operaton-remote-service-task-initial-pull")
  @Qualifier("operaton-remote-service-task-initial-pull")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.execute-initial-pull-on-startup"])
  fun configureInitialPullForExternalServiceTaskDelivery(
    lifecycle: ProcessApplicationLifecycle,
    externalTaskApi: ExternalTaskApiClient,
    subscriptionRepository: SubscriptionRepository,
    operatonAdapterProperties: OperatonRemoteAdapterProperties,
    @Qualifier("operaton-remote-service-task-worker-executor")
    executor: ThreadPoolExecutor,
    valueMapper: ValueMapper,
    @Qualifier("operaton-remote-process-definition-meta-data-resolver")
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
    metrics: PullServiceTaskDeliveryMetrics,
  ) = OperatonRemoteInitialPullServiceTasksDeliveryBinding(
    externalTaskApiClient = externalTaskApi,
    subscriptionRepository = subscriptionRepository,
    operatonAdapterProperties = operatonAdapterProperties,
    executor = executor,
    valueMapper = valueMapper,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    metrics = metrics,
  ).also { binding ->
    lifecycle.registerParticipant(ProcessApplicationPhase.RUNNING - 20) { binding.pullServiceTasks() }
  }
}
