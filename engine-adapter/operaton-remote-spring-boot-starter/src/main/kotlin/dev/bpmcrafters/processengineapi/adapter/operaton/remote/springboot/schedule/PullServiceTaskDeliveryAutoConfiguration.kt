package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemotePullServicesAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.ConditionalOnServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.registerParticipant
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDelivery
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
  strategy = REMOTE_SCHEDULED
)
@AutoConfigureAfter(OperatonRemotePullServicesAutoConfiguration::class)
class PullServiceTaskDeliveryAutoConfiguration(
  private val pullServiceTaskDelivery: PullServiceTaskDelivery,
  private val operatonRemoteAdapterProperties: OperatonRemoteAdapterProperties,
  @Qualifier("operaton-remote-task-scheduler")
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

    val deliveryInterval = Duration.ofMillis(operatonRemoteAdapterProperties.serviceTasks.scheduleDeliveryIntervalInMilliseconds)
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
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-105: Delivering external tasks..." }
    pullServiceTaskDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-106: Delivered external tasks." }
  }

}
