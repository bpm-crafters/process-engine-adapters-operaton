package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties.ExternalServiceTaskDeliveryStrategy.EMBEDDED_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.ConditionalOnServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit.SECONDS

private val logger = KotlinLogging.logger {}

/**
 * Dynamic / imperative scheduling configuration using own task scheduler for service tasks.
 *
 * This class stays separate from the bean factory configuration so `@Scheduled` is only registered
 * when the embedded scheduled delivery strategy is selected.
 */
@AutoConfiguration
@ConditionalOnServiceTaskDeliveryStrategy(
  strategy = EMBEDDED_SCHEDULED
)
@AutoConfigureAfter(OperatonEmbeddedSchedulingAutoConfiguration::class)
class OperatonEmbeddedServiceTaskPullStrategyAutoConfiguration(
  private val embeddedPullServiceTaskDelivery: EmbeddedPullServiceTaskDelivery
) {

  @Scheduled(
    fixedDelayString = "#{@'dev.bpm-crafters.process-api.adapter.operaton-embedded-dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties'.serviceTasks.scheduleDeliveryFixedRateInSeconds}",
    timeUnit = SECONDS,
    scheduler = "operaton-embedded-task-scheduler"
  )
  fun refresh() {
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-105: Delivering external tasks..." }
    embeddedPullServiceTaskDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-OPERATON-EMBEDDED-106: Delivered external tasks." }
  }

}
