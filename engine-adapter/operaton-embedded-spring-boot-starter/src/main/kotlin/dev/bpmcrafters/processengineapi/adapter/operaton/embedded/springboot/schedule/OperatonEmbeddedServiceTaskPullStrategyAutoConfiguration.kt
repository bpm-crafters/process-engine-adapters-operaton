package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties.ExternalServiceTaskDeliveryStrategy.EMBEDDED_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.ConditionalOnServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.registerParticipant
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
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
 * Dynamic / imperative scheduling configuration using own task scheduler for service tasks.
 */
@AutoConfiguration
@ConditionalOnServiceTaskDeliveryStrategy(
  strategy = EMBEDDED_SCHEDULED
)
@AutoConfigureAfter(OperatonEmbeddedSchedulingAutoConfiguration::class)
class OperatonEmbeddedServiceTaskPullStrategyAutoConfiguration(
  private val embeddedPullServiceTaskDelivery: EmbeddedPullServiceTaskDelivery,
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

    val deliveryInterval = Duration.ofMillis(operatonEmbeddedAdapterProperties.serviceTasks.scheduleDeliveryIntervalInMilliseconds)
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
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-105: Delivering external tasks..." }
    embeddedPullServiceTaskDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-106: Delivered external tasks." }
  }

}
