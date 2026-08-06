package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties.UserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.ConditionalOnUserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit.SECONDS

private val logger = KotlinLogging.logger {}

/**
 * Dynamic / imperative scheduling configuration using own task scheduler for user tasks.
 *
 * Keeping the scheduled trigger in its own auto-configuration prevents the scheduled polling hook
 * from being registered for non-scheduled user-task delivery strategies.
 */
@AutoConfiguration
@ConditionalOnUserTaskDeliveryStrategy(
  strategies = [ UserTaskDeliveryStrategy.EMBEDDED_SCHEDULED ]
)
@AutoConfigureAfter(OperatonEmbeddedSchedulingAutoConfiguration::class)
class OperatonEmbeddedUserTaskPullStrategyAutoConfiguration(
  private val embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery
) {

  @Scheduled(
    fixedDelayString = "#{@'dev.bpm-crafters.process-api.adapter.operaton-embedded-dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties'.userTasks.scheduleDeliveryFixedRateInSeconds}",
    timeUnit = SECONDS,
    scheduler = "operaton-embedded-task-scheduler"
  )
  fun refresh() {
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-107: Delivering user tasks..." }
    embeddedPullUserTaskDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-108: Delivered user tasks." }
  }

}
