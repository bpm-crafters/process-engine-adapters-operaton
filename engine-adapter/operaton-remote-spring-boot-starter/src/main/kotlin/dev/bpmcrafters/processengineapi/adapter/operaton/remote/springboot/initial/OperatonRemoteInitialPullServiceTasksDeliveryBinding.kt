package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.initial.OperatonRemoteInitialPullServiceTasksDeliveryBinding.Companion.ORDER
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.variables.ValueMapper
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.ThreadPoolExecutor

private val logger = KotlinLogging.logger {}

/**
 * This class is responsible for the initial pull of user tasks.
 * We are not relying on the pull delivery strategy configured centrally, because for other deliveries we still want to
 * execute an initial pull.
 */
@Order(ORDER)
open class OperatonRemoteInitialPullServiceTasksDeliveryBinding(
  externalTaskApiClient: ExternalTaskApiClient,
  subscriptionRepository: SubscriptionRepository,
  adapterProperties: OperatonRemoteAdapterProperties,
  executor: ThreadPoolExecutor,
  valueMapper: ValueMapper,
  processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
  metrics: PullServiceTaskDeliveryMetrics
) {
  companion object {
    const val ORDER = Ordered.HIGHEST_PRECEDENCE + 1000
  }

  private val pullDelivery = PullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskApiClient = externalTaskApiClient,
    workerId = adapterProperties.serviceTasks.workerId,
    maxTasks = adapterProperties.serviceTasks.maxTaskCount,
    lockDurationInSeconds = adapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeoutInSeconds = adapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = adapterProperties.serviceTasks.retries,
    executor = executor,
    valueMapper = valueMapper,
    deserializeOnServer = adapterProperties.serviceTasks.deserializeOnServer,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    metrics = metrics
  )

  @EventListener
  @Async
  open fun pullUserTasks(event: ApplicationStartedEvent) {
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-101: Delivering service tasks..." }
    pullDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-102: Delivered service tasks." }
  }

}
