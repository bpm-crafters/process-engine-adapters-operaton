package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.ExternalTaskService
import java.util.concurrent.ThreadPoolExecutor

private val logger = KotlinLogging.logger {}

/**
 * This class is responsible for the initial pull of service tasks.
 * We are not relying on the pull delivery strategy configured centrally, because for other deliveries we still want to
 * execute an initial pull (e.g. for event-based delivery)
 */
open class OperatonEmbeddedInitialPullServiceTasksDeliveryBinding(
  externalTaskService: ExternalTaskService,
  subscriptionRepository: SubscriptionRepository,
  private val operatonAdapterProperties: OperatonEmbeddedAdapterProperties,
  private val executor: ThreadPoolExecutor,
  metrics: EmbeddedPullServiceTaskDeliveryMetrics
) {

  private val pullDelivery = EmbeddedPullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = operatonAdapterProperties.serviceTasks.workerId,
    maxTasks = operatonAdapterProperties.serviceTasks.maxTaskCount,
    lockDurationInSeconds = operatonAdapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeoutInSeconds = operatonAdapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = operatonAdapterProperties.serviceTasks.retries,
    executor = executor,
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
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-101: Delivering service tasks..." }
    pullDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-102: Delivered service tasks." }
  }

}
