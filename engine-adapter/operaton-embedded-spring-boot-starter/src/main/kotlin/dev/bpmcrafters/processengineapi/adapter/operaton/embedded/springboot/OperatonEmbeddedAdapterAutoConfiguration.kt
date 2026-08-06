package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.common.serialization.AdapterDataConverter
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.decision.EvaluateDecisionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.shared.EngineCommandExecutor
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.schedule.DefaultPullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.completion.OperatonServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.completion.OperatonUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.completion.LinearMemoryFailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.NoOpPullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.modification.OperatonUserTaskModificationApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.subscription.OperatonTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.decision.EvaluateDecisionApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.impl.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import io.toolisticon.spring.condition.ConditionalOnMissingQualifiedBean
import jakarta.annotation.PostConstruct
import org.operaton.bpm.engine.*
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

/**
 * Registers the BPM Crafters API beans that adapt the in-process Operaton engine services.
 *
 * The class is declared as an auto-configuration because it is listed in
 * `AutoConfiguration.imports`; the adapter enabled condition intentionally applies only to these
 * adapter-facing beans, not to Operaton's own engine auto-configuration.
 */
@AutoConfiguration
@EnableConfigurationProperties(value = [OperatonEmbeddedAdapterProperties::class])
@Conditional(OperatonEmbeddedAdapterEnabledCondition::class)
class OperatonEmbeddedAdapterAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-OPERATON-EMBEDDED-200: Configuration of services applied." }
  }

  @Bean
  @ConditionalOnMissingBean
  fun engineCommandExecutor(): EngineCommandExecutor = EngineCommandExecutor()

  @Bean("operaton-embedded-start-process-api")
  @Qualifier("operaton-embedded-start-process-api")
  fun startProcessApi(
    runtimeService: RuntimeService,
    repositoryService: RepositoryService,
    commandExecutor: EngineCommandExecutor,
  ): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService,
    repositoryService = repositoryService,
    commandExecutor = commandExecutor,
  )

  @Bean("operaton-embedded-task-subscription-api")
  @Qualifier("operaton-embedded-task-subscription-api")
  fun taskSubscriptionApi(subscriptionRepository: SubscriptionRepository): TaskSubscriptionApi = OperatonTaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository
  )

  @Bean("operaton-embedded-correlation-api")
  @Qualifier("operaton-embedded-correlation-api")
  fun correlationApi(
    runtimeService: RuntimeService,
    commandExecutor: EngineCommandExecutor,
  ): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService,
    commandExecutor = commandExecutor,
  )

  @Bean("operaton-embedded-signal-api")
  @Qualifier("operaton-embedded-signal-api")
  fun signalApi(
    runtimeService: RuntimeService,
    commandExecutor: EngineCommandExecutor,
  ): SignalApi = SignalApiImpl(
    runtimeService = runtimeService,
    commandExecutor = commandExecutor,
  )

  @Bean("operaton-embedded-deployment-api")
  @Qualifier("operaton-embedded-deployment-api")
  fun deploymentApi(
    repositoryService: RepositoryService,
    commandExecutor: EngineCommandExecutor,
  ): DeploymentApi = DeploymentApiImpl(
    repositoryService = repositoryService,
    commandExecutor = commandExecutor,
  )

  @Bean("operaton-embedded-user-task-modification-api")
  @Qualifier("operaton-embedded-user-task-modification-api")
  fun userTaskModificationApi(
    taskService: TaskService,
    commandExecutor: EngineCommandExecutor,
  ): UserTaskModificationApi = OperatonUserTaskModificationApiImpl(
    taskService = taskService,
    commandExecutor = commandExecutor,
  )

  @Bean("operaton-embedded-evaluate-decision-api")
  @Qualifier("operaton-embedded-evaluate-decision-api")
  fun evaluateDecisionApi(
    decisionService: DecisionService,
    dataConverter: AdapterDataConverter,
    commandExecutor: EngineCommandExecutor
  ): EvaluateDecisionApi = EvaluateDecisionApiImpl(
    decisionService = decisionService,
    dataConverter = dataConverter,
    commandExecutor = commandExecutor
  )

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(MeterRegistry::class)
  fun defaultEmbeddedPullServiceTaskDeliveryMetrics(registry: MeterRegistry): EmbeddedPullServiceTaskDeliveryMetrics =
    DefaultPullServiceTaskDeliveryMetrics(registry)

  @Bean
  @ConditionalOnMissingBean(EmbeddedPullServiceTaskDeliveryMetrics::class, MeterRegistry::class)
  fun noOpEmbeddedPullServiceTaskDeliveryMetrics(): EmbeddedPullServiceTaskDeliveryMetrics =
    NoOpPullServiceTaskDeliveryMetrics()


  @Bean
  @ConditionalOnMissingBean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  @Bean("operaton-embedded-failure-retry-supplier")
  @Qualifier("operaton-embedded-failure-retry-supplier")
  @ConditionalOnMissingBean
  fun defaultFailureRetrySupplier(adapterProperties: OperatonEmbeddedAdapterProperties): FailureRetrySupplier {
    return LinearMemoryFailureRetrySupplier(
      retry = adapterProperties.serviceTasks.retries,
      retryTimeout = adapterProperties.serviceTasks.retryTimeoutInSeconds
    )
  }

  @Bean("operaton-embedded-service-task-completion-api")
  @Qualifier("operaton-embedded-service-task-completion-api")
  fun serviceTaskCompletionApi(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    adapterProperties: OperatonEmbeddedAdapterProperties,
    @Qualifier("operaton-embedded-failure-retry-supplier")
    failureRetrySupplier: FailureRetrySupplier,
    commandExecutor: EngineCommandExecutor
  ): ServiceTaskCompletionApi =
    OperatonServiceTaskCompletionApiImpl(
      workerId = adapterProperties.serviceTasks.workerId,
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository,
      failureRetrySupplier = failureRetrySupplier,
      commandExecutor = commandExecutor,

    )

  @Bean("operaton-embedded-user-task-completion-api")
  @Qualifier("operaton-embedded-user-task-completion-api")
  fun userTaskCompletionApi(
    taskService: TaskService,
    subscriptionRepository: SubscriptionRepository,
    commandExecutor: EngineCommandExecutor
  ): UserTaskCompletionApi =
    OperatonUserTaskCompletionApiImpl(
      taskService = taskService,
      subscriptionRepository = subscriptionRepository,
      commandExecutor = commandExecutor,
    )

  /**
   * Creates a default fixed thread pool for 10 threads used for process engine worker executions.
   * This one is used for pull-strategies only.
   */
  @Bean("operaton-embedded-service-task-worker-executor")
  @ConditionalOnMissingQualifiedBean(beanClass = ExecutorService::class, qualifier = "operaton-embedded-service-task-worker-executor")
  @Qualifier("operaton-embedded-service-task-worker-executor")
  fun serviceTaskWorkerExecutor(): ExecutorService = Executors.newFixedThreadPool(10)

  /**
   * Creates a default fixed thread pool for 10 threads used for process engine worker executions.
   * This one is used for pull-strategies and async event listener execution.
   */
  @Bean("operaton-embedded-user-task-worker-executor")
  @ConditionalOnMissingQualifiedBean(beanClass = ExecutorService::class, qualifier = "operaton-embedded-user-task-worker-executor")
  @Qualifier("operaton-embedded-user-task-worker-executor")
  fun userTaskWorkerExecutor(): ExecutorService = Executors.newFixedThreadPool(10)

}
