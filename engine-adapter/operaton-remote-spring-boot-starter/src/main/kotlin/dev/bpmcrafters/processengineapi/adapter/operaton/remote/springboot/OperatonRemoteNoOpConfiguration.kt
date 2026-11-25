package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.NoOpServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional

private val logger = KotlinLogging.logger {}

@AutoConfiguration
@AutoConfigureAfter(OperatonRemoteAdapterAutoConfiguration::class)
@Conditional(OperatonRemoteAdapterEnabledCondition::class)
class OperatonRemoteNoOpConfiguration {

  @ConditionalOnServiceTaskDeliveryStrategy(
    strategy = OperatonRemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.DISABLED
  )
  @Bean
  fun noOpServiceTaskCompletionApi(): ServiceTaskCompletionApi {
    logger.info { "PROCESS-ENGINE-OPERATON-REMOTE-211: Configured a NO-OP ServiceTaskCompletion API, no service task completion is possible." }
    return NoOpServiceTaskCompletionApiImpl()
  }
}
