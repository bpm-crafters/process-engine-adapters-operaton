package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.CachingProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.FeignServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.UserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDeliveryMetrics
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.modification.UserTaskModificationApiImpl
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi
import io.github.oshai.kotlinlogging.KotlinLogging
import io.toolisticon.spring.condition.ConditionalOnMissingQualifiedBean
import jakarta.annotation.PostConstruct
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient
import org.camunda.community.rest.client.api.TaskApiClient
import org.camunda.community.rest.variables.ValueMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.Order
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

private val logger = KotlinLogging.logger {}

/**
 * Autoconfiguration for scheduled delivery.
 */
@AutoConfiguration
@EnableAsync
@EnableScheduling
@AutoConfigureAfter(OperatonRemoteAdapterAutoConfiguration::class)
@Conditional(OperatonRemoteAdapterEnabledCondition::class)
class OperatonRemotePullServicesAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-OPERATON-REMOTE-202: Configuration applied." }
  }

  @Bean("operaton-remote-task-scheduler")
  @Qualifier("operaton-remote-task-scheduler")
  @Order(200)
  @ConditionalOnMissingQualifiedBean(beanClass = TaskScheduler::class, qualifier = "operaton-remote-task-scheduler")
  fun taskScheduler(): TaskScheduler {
    val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
    threadPoolTaskScheduler.poolSize = 2 // we have two schedulers, one for user tasks one for service tasks
    threadPoolTaskScheduler.setThreadNamePrefix("OPERATON-REMOTE-SCHEDULER-")
    return threadPoolTaskScheduler
  }

  @Bean("taskScheduler")
  @Order(100)
  @Conditional(VirtualThreadingCondition::class)
  fun taskSchedulerVirtualThreads(builder: SimpleAsyncTaskSchedulerBuilder): SimpleAsyncTaskScheduler {
    return builder.build()
  }

  @Bean("taskScheduler")
  @Order(100)
  @Conditional(PlatformThreadingCondition::class)
  fun taskSchedulerPlatformThreads(threadPoolTaskSchedulerBuilder: ThreadPoolTaskSchedulerBuilder): ThreadPoolTaskScheduler {
    return threadPoolTaskSchedulerBuilder.build()
  }

  @Bean("operaton-remote-service-task-delivery")
  @Qualifier("operaton-remote-service-task-delivery")
  @ConditionalOnServiceTaskDeliveryStrategy(
    strategy = OperatonRemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
  )
  fun scheduledServiceTaskDelivery(
    externalTaskApiClient: ExternalTaskApiClient,
    @Qualifier("operaton-remote-process-definition-meta-data-resolver")
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
    subscriptionRepository: SubscriptionRepository,
    adapterProperties: OperatonRemoteAdapterProperties,
    @Qualifier("operaton-remote-service-task-worker-executor")
    executor: ThreadPoolExecutor,
    valueMapper: ValueMapper,
    metrics: PullServiceTaskDeliveryMetrics
  ) = PullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    workerId = adapterProperties.serviceTasks.workerId,
    maxTasks = adapterProperties.serviceTasks.maxTaskCount,
    lockDurationInSeconds = adapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeoutInSeconds = adapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = adapterProperties.serviceTasks.retries,
    executor = executor,
    externalTaskApiClient = externalTaskApiClient,
    processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
    valueMapper = valueMapper,
    deserializeOnServer = adapterProperties.serviceTasks.deserializeOnServer,
    metrics = metrics
  )

  @Bean("operaton-remote-service-task-completion-api")
  @Qualifier("operaton-remote-service-task-completion-api")
  @ConditionalOnServiceTaskDeliveryStrategy(
    strategy = OperatonRemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
  )
  fun scheduledServiceTaskCompletionApi(
    externalTaskApiClient: ExternalTaskApiClient,
    subscriptionRepository: SubscriptionRepository,
    adapterProperties: OperatonRemoteAdapterProperties,
    @Qualifier("operaton-remote-failure-retry-supplier")
    failureRetrySupplier: FailureRetrySupplier,
    valueMapper: ValueMapper
  ): ServiceTaskCompletionApi =
    FeignServiceTaskCompletionApiImpl(
      workerId = adapterProperties.serviceTasks.workerId,
      externalTaskApiClient = externalTaskApiClient,
      subscriptionRepository = subscriptionRepository,
      failureRetrySupplier = failureRetrySupplier,
      valueMapper = valueMapper
    )

  @Bean("operaton-remote-process-definition-meta-data-resolver")
  @Qualifier("operaton-remote-process-definition-meta-data-resolver")
  @ConditionalOnMissingQualifiedBean(beanClass = ProcessDefinitionMetaDataResolver::class, qualifier = "operaton-remote-process-definition-meta-data-resolver")
  fun cachingProcessDefinitionMetaDataResolver(processDefinitionApiClient: ProcessDefinitionApiClient): ProcessDefinitionMetaDataResolver {
    return CachingProcessDefinitionMetaDataResolver(processDefinitionApiClient)
  }

  @Bean("operaton-remote-user-task-delivery")
  @Qualifier("operaton-remote-user-task-delivery")
  @ConditionalOnUserTaskDeliveryStrategy(
    strategy = OperatonRemoteAdapterProperties.UserTaskDeliveryStrategy.REMOTE_SCHEDULED
  )
  fun scheduledUserTaskDelivery(
    @Qualifier("operaton-remote-process-definition-meta-data-resolver")
    processDefinitionMetaDataResolver: ProcessDefinitionMetaDataResolver,
    taskApiClient: TaskApiClient,
    subscriptionRepository: SubscriptionRepository,
    adapterProperties: OperatonRemoteAdapterProperties,
    @Qualifier("operaton-remote-user-task-worker-executor")
    executorService: ExecutorService,
    valueMapper: ValueMapper
  ): PullUserTaskDelivery {
    return PullUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      executorService = executorService,
      valueMapper = valueMapper,
      processDefinitionMetaDataResolver = processDefinitionMetaDataResolver,
      taskApiClient = taskApiClient,
      deserializeOnServer = adapterProperties.userTasks.deserializeOnServer
    )
  }

  /**
   * User task completion API.
   */
  @Bean("operaton-remote-user-task-completion-api")
  @Qualifier("operaton-remote-user-task-completion-api")
  @ConditionalOnUserTaskDeliveryStrategy(
    strategy = OperatonRemoteAdapterProperties.UserTaskDeliveryStrategy.REMOTE_SCHEDULED
  )
  fun userTaskCompletionApi(
    taskApiClient: TaskApiClient,
    subscriptionRepository: SubscriptionRepository,
    valueMapper: ValueMapper,
  ): UserTaskCompletionApi =
    UserTaskCompletionApiImpl(
      taskApiClient = taskApiClient,
      subscriptionRepository = subscriptionRepository,
      valueMapper = valueMapper
    )

  /**
   * User task modification api.
   */
  @Bean("operaton-remote-user-task-modification-api")
  @Qualifier("operaton-remote-user-task-modification-api")
  @ConditionalOnUserTaskDeliveryStrategy(
    strategy = OperatonRemoteAdapterProperties.UserTaskDeliveryStrategy.REMOTE_SCHEDULED
  )
  fun userTaskModificationApi(
    taskApiClient: TaskApiClient,
    valueMapper: ValueMapper
  ): UserTaskModificationApi =
    UserTaskModificationApiImpl(
      taskApiClient = taskApiClient,
      valueMapper = valueMapper
    )
}
