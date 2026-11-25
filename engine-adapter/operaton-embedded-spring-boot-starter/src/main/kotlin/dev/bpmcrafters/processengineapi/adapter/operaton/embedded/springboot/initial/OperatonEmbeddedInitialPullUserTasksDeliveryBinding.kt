package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.TaskService
import java.util.concurrent.ExecutorService

private val logger = KotlinLogging.logger {}

/**
 * This class is responsible for the initial pull of user tasks.
 * We are not relying on the pull delivery strategy configured centrally, because for other deliveries we still want to
 * execute an initial pull (e.g. for event-based delivery)
 */
open class OperatonEmbeddedInitialPullUserTasksDeliveryBinding(
    subscriptionRepository: SubscriptionRepository,
    taskService: TaskService,
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
    private val executorService: ExecutorService,
    private val operatonEmbeddedAdapterProperties: OperatonEmbeddedAdapterProperties
) {

  private val pullDelivery = EmbeddedPullUserTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    taskService = taskService,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    executorService = executorService
  )

  open fun pullUserTasks() {
    if (operatonEmbeddedAdapterProperties.userTasks.executeInitialPullAsync) {
      executorService.execute { deliverUserTasks() }
    } else {
      deliverUserTasks()
    }
  }

  private fun deliverUserTasks() {
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-103: Delivering user tasks..." }
    pullDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-104: Delivered user tasks." }
  }
}
