package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.initial.OperatonRemoteInitialPullUserTasksDeliveryBinding.Companion.ORDER
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.community.rest.client.api.TaskApiClient
import org.camunda.community.rest.variables.ValueMapper
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.ExecutorService

private val logger = KotlinLogging.logger {}

/**
 * This class is responsible for the initial pull of user tasks.
 * We are not relying on the pull delivery strategy configured centrally, because for other deliveries we still want to
 * execute an initial pull (e.g. for event-based delivery)
 */
@Order(ORDER)
open class OperatonRemoteInitialPullUserTasksDeliveryBinding(
  subscriptionRepository: SubscriptionRepository,
  processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
  taskApiClient: TaskApiClient,
  executorService: ExecutorService,
  valueMapper: ValueMapper,
  adapterProperties: OperatonRemoteAdapterProperties
) {

  companion object {
    const val ORDER = Ordered.HIGHEST_PRECEDENCE + 2000
  }

  private val pullUserTaskDelivery = PullUserTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    executorService = executorService,
    valueMapper = valueMapper,
    taskApiClient = taskApiClient,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    deserializeOnServer = adapterProperties.userTasks.deserializeOnServer
  )

  @EventListener
  @Async
  open fun pullUserTasks(event: ApplicationStartedEvent) {
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-103: Delivering user tasks..." }
    pullUserTaskDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-104: Delivered user tasks." }
  }
}
