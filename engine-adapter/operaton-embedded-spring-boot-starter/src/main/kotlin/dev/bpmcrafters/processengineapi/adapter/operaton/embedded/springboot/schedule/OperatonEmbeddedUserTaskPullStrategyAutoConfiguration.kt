package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties.UserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.ConditionalOnUserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.registerParticipant
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.scheduling.TaskScheduler
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ScheduledFuture

private val logger = KotlinLogging.logger {}

/**
 * Dynamic / imperative scheduling configuration using own task scheduler for user tasks.
 */
@AutoConfiguration
@ConditionalOnUserTaskDeliveryStrategy(
  strategies = [ UserTaskDeliveryStrategy.EMBEDDED_SCHEDULED ]
)
@AutoConfigureAfter(OperatonEmbeddedSchedulingAutoConfiguration::class)
class OperatonEmbeddedUserTaskPullStrategyAutoConfiguration(
  private val embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery,
  private val operatonEmbeddedAdapterProperties: OperatonEmbeddedAdapterProperties,
  @Qualifier("operaton-embedded-task-scheduler")
  private val taskScheduler: TaskScheduler,
  lifecycle: ProcessApplicationLifecycle
) {
  private var scheduledDelivery: ScheduledFuture<*>? = null

  init {
    lifecycle.registerParticipant(ProcessApplicationPhase.RUNNING) { start() }
    lifecycle.registerParticipant(ProcessApplicationPhase.STOPPING) { stop() }
  }

  @Synchronized
  fun start() {
    if (scheduledDelivery != null) {
      return
    }

    val deliveryInterval = Duration.ofMillis(operatonEmbeddedAdapterProperties.userTasks.scheduleDeliveryIntervalInMilliseconds)
    scheduledDelivery = taskScheduler.scheduleWithFixedDelay(
      { refresh() },
      Instant.now().plus(deliveryInterval),
      deliveryInterval
    )
  }

  @Synchronized
  fun stop() {
    scheduledDelivery?.cancel(false)
    scheduledDelivery = null
  }

  private fun refresh() {
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-107: Delivering user tasks..." }
    embeddedPullUserTaskDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-108: Delivered user tasks." }
  }

}
