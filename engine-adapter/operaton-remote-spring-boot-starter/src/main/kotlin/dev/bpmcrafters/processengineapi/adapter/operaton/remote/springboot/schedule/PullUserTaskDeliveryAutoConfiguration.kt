package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties.UserTaskDeliveryStrategy.REMOTE_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemotePullServicesAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.ConditionalOnUserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullUserTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit.SECONDS

private val logger = KotlinLogging.logger {}

/**
 * Dynamic / imperative scheduling configuration using own task scheduler for user tasks.
 */
@AutoConfiguration
@ConditionalOnUserTaskDeliveryStrategy(
  strategy = REMOTE_SCHEDULED
)
@AutoConfigureAfter(OperatonRemotePullServicesAutoConfiguration::class)
class PullUserTaskDeliveryAutoConfiguration(
  private val remotePullUserTaskDelivery: PullUserTaskDelivery
) {

  @Scheduled(
    fixedDelayString = "#{@'dev.bpm-crafters.process-api.adapter.operaton-remote-dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties'.userTasks.scheduleDeliveryFixedRateInSeconds}",
    timeUnit = SECONDS,
    scheduler = "operaton-remote-task-scheduler"
  )
  fun refresh() {
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-107: Delivering user tasks..." }
    remotePullUserTaskDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-REMOTE-108: Delivered user tasks." }
  }

}
