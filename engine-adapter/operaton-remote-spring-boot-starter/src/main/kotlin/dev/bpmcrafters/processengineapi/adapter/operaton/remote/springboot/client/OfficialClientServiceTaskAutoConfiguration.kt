package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.client

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SUBSCRIBED
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.ConditionalOnServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.OfficialClientServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.subscribe.SubscribingServiceTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.impl.ExternalTaskClientImpl
import org.camunda.bpm.client.task.ExternalTaskService
import org.camunda.bpm.client.task.impl.ExternalTaskServiceImpl
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean

private val logger = KotlinLogging.logger {}

/**
 * Auto-configuration for subscribed delivery using Camunda External Client.
 */
@AutoConfiguration
@AutoConfigureAfter(OperatonRemoteAdapterAutoConfiguration::class)
@ConditionalOnServiceTaskDeliveryStrategy(
  strategy = REMOTE_SUBSCRIBED
)
class OfficialClientServiceTaskAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-OPERATON-REMOTE-201: Configuration applied." }
  }

  @Bean
  fun externalTaskClientExternalTaskService(externalTaskClient: ExternalTaskClient): ExternalTaskService {
    require(externalTaskClient is ExternalTaskClientImpl) { "External task client must be official Camunda External Task Client" }
    return ExternalTaskServiceImpl(externalTaskClient.topicSubscriptionManager.engineClient)
  }

  @Bean(name = ["operaton-remote-service-task-delivery"], initMethod = "subscribe", destroyMethod = "unsubscribe")
  fun subscribingClientExternalTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    externalTaskClient: ExternalTaskClient,
    adapterProperties: OperatonRemoteAdapterProperties
  ) = SubscribingServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    lockDurationInSeconds = adapterProperties.serviceTasks.lockTimeInSeconds,
    externalTaskClient = externalTaskClient,
    retryTimeoutInSeconds = adapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = adapterProperties.serviceTasks.retries
  )

  @Bean("operaton-remote-service-task-completion-api")
  @Qualifier("operaton-remote-service-task-completion-api")
  fun externalTaskClientCompletionApi(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    @Qualifier("operaton-remote-failure-retry-supplier")
    failureRetrySupplier: FailureRetrySupplier
  ): ServiceTaskCompletionApi =
    OfficialClientServiceTaskCompletionApiImpl(
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository,
      failureRetrySupplier = failureRetrySupplier
    )

}
