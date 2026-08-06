package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemotePullServicesAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.ConditionalOnServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit.SECONDS

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
  private val pullServiceTaskDelivery: PullServiceTaskDelivery
) {

  @Scheduled(
    fixedDelayString = "#{@'dev.bpm-crafters.process-api.adapter.operaton-remote-dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties'.serviceTasks.scheduleDeliveryFixedRateInSeconds}",
    timeUnit = SECONDS,
    scheduler = "operaton-remote-task-scheduler"
  )
  fun refresh() {
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-105: Delivering external tasks..." }
    pullServiceTaskDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-106: Delivered external tasks." }
  }

}
