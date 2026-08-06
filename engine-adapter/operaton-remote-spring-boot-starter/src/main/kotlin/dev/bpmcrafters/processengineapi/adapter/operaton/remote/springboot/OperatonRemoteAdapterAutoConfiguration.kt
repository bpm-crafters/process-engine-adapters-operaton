package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.common.serialization.AdapterDataConverter
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.decision.EvaluateDecisionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.schedule.DefaultPullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.schedule.NoOpPullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.LinearMemoryFailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.decision.EvaluateDecisionApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.impl.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import io.toolisticon.spring.condition.ConditionalOnMissingQualifiedBean
import jakarta.annotation.PostConstruct
import org.camunda.community.rest.client.api.*
import org.camunda.community.rest.variables.ValueMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import java.util.concurrent.*

private val logger = KotlinLogging.logger {}

@AutoConfiguration(afterName = ["org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration"])
@EnableConfigurationProperties(value = [OperatonRemoteAdapterProperties::class])
@Conditional(OperatonRemoteAdapterEnabledCondition::class)
class OperatonRemoteAdapterAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-OPERATON-REMOTE-200: Configuration applied." }
  }

  @Bean("operaton-remote-task-subscription-api")
  @Qualifier("operaton-remote-task-subscription-api")
  fun taskSubscriptionApi(subscriptionRepository: SubscriptionRepository): TaskSubscriptionApi = TaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository
  )

  @Bean("operaton-remote-start-process-api")
  @Qualifier("operaton-remote-start-process-api")
  fun startProcessApi(
    processDefinitionApiClient: ProcessDefinitionApiClient,
    messageApiClient: MessageApiClient,
    processInstanceApiClient: ProcessInstanceApiClient,
    valueMapper: ValueMapper,
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
  ): StartProcessApi = StartProcessApiImpl(
    processDefinitionApiClient = processDefinitionApiClient,
    messageApiClient = messageApiClient,
    processInstanceApiClient = processInstanceApiClient,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    valueMapper = valueMapper
  )

  @Bean("operaton-remote-correlation-api")
  @Qualifier("operaton-remote-correlation-api")
  fun correlationApi(messageApiClient: MessageApiClient, valueMapper: ValueMapper): CorrelationApi = CorrelationApiImpl(
    messageApiClient = messageApiClient,
    valueMapper = valueMapper
  )

  @Bean("operaton-remote-signal-api")
  @Qualifier("operaton-remote-signal-api")
  fun signalApi(signalApiClient: SignalApiClient, valueMapper: ValueMapper): SignalApi = SignalApiImpl(
    signalApiClient = signalApiClient,
    valueMapper = valueMapper
  )

  @Bean("operaton-remote-deploy-api")
  @Qualifier("operaton-remote-deploy-api")
  fun deployApi(deploymentApiClient: DeploymentApiClient): DeploymentApi = DeploymentApiImpl(
    deploymentApiClient = deploymentApiClient
  )

  @Bean("operaton-remote-evaluate-decision-api")
  @Qualifier("operaton-remote-evaluate-decision-api")
  fun evaluateDecisionApi(decisionDefinitionApiClient: DecisionDefinitionApiClient, valueMapper: ValueMapper,
                          dataConverter: AdapterDataConverter): EvaluateDecisionApi = EvaluateDecisionApiImpl(
    decisionDefinitionApiClient = decisionDefinitionApiClient,
    valueMapper = valueMapper,
    dataConverter = dataConverter
  )

  /**
   * Subscription Repository.
   */
  @Bean
  @ConditionalOnMissingBean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  /**
   * Creates a default fixed thread pool used for external task worker executions.
   * This one is used for pull-strategies only.
   */
  @Bean("operaton-remote-service-task-worker-executor")
  @Qualifier("operaton-remote-service-task-worker-executor")
  @ConditionalOnMissingQualifiedBean(beanClass = ThreadPoolExecutor::class, qualifier = "operaton-remote-service-task-worker-executor")
  fun serviceTaskWorkerExecutor(adapterProperties: OperatonRemoteAdapterProperties): ThreadPoolExecutor =
    ThreadPoolExecutor(
      adapterProperties.serviceTasks.workerThreadPoolSize,
      adapterProperties.serviceTasks.workerThreadPoolSize,
      0L, TimeUnit.MILLISECONDS,
      LinkedBlockingQueue(adapterProperties.serviceTasks.workerThreadPoolQueueCapacity)
    )

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(MeterRegistry::class)
  fun defaultPullServiceTaskDeliveryMetrics(registry: MeterRegistry): PullServiceTaskDeliveryMetrics =
    DefaultPullServiceTaskDeliveryMetrics(registry)

  @Bean
  @ConditionalOnMissingBean(PullServiceTaskDeliveryMetrics::class, MeterRegistry::class)
  fun noOpPullServiceTaskDeliveryMetrics(): PullServiceTaskDeliveryMetrics =
    NoOpPullServiceTaskDeliveryMetrics()

  /**
   * Creates a default fixed thread pool for 10 threads used for process engine worker executions.
   * This one is used for pull-strategies only.
   */
  @Bean("operaton-remote-user-task-worker-executor")
  @Qualifier("operaton-remote-user-task-worker-executor")
  @ConditionalOnMissingQualifiedBean(beanClass = ExecutorService::class, qualifier = "operaton-remote-user-task-worker-executor")
  fun userTaskWorkerExecutor(): ExecutorService = Executors.newFixedThreadPool(10)

  /**
   * Failure retry supplier.
   */
  @Bean("operaton-remote-failure-retry-supplier")
  @Qualifier("operaton-remote-failure-retry-supplier")
  @ConditionalOnMissingBean
  fun defaultFailureRetrySupplier(adapterProperties: OperatonRemoteAdapterProperties): FailureRetrySupplier {
    return LinearMemoryFailureRetrySupplier(
      retry = adapterProperties.serviceTasks.retries,
      retryTimeout = adapterProperties.serviceTasks.retryTimeoutInSeconds
    )
  }

}
