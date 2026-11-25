package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.variables.ValueMapper
import java.util.concurrent.ThreadPoolExecutor

private val logger = KotlinLogging.logger {}

/**
 * This class is responsible for the initial pull of service tasks.
 * We are not relying on the pull delivery strategy configured centrally, because for other deliveries we still want to
 * execute an initial pull.
 */
open class OperatonRemoteInitialPullServiceTasksDeliveryBinding(
  externalTaskApiClient: ExternalTaskApiClient,
  subscriptionRepository: SubscriptionRepository,
  private val operatonAdapterProperties: OperatonRemoteAdapterProperties,
  private val executor: ThreadPoolExecutor,
  valueMapper: ValueMapper,
  processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
  metrics: PullServiceTaskDeliveryMetrics
) {
  private val pullDelivery = PullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskApiClient = externalTaskApiClient,
    workerId = operatonAdapterProperties.serviceTasks.workerId,
    maxTasks = operatonAdapterProperties.serviceTasks.maxTaskCount,
    lockDurationInSeconds = operatonAdapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeoutInSeconds = operatonAdapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = operatonAdapterProperties.serviceTasks.retries,
    executor = executor,
    valueMapper = valueMapper,
    deserializeOnServer = operatonAdapterProperties.serviceTasks.deserializeOnServer,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    metrics = metrics
  )

  open fun pullServiceTasks() {
    if (operatonAdapterProperties.serviceTasks.executeInitialPullAsync) {
      executor.execute { deliverServiceTasks() }
    } else {
      deliverServiceTasks()
    }
  }

  private fun deliverServiceTasks() {
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-101: Delivering service tasks..." }
    pullDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-102: Delivered service tasks." }
  }

}
