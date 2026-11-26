package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process.CachingProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.shared.EngineCommandExecutor
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.completion.LinearMemoryFailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.completion.OperatonServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.completion.OperatonUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.modification.OperatonUserTaskModificationApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.subscription.OperatonTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.impl.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestInitializationRequest
import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestInitializer
import org.operaton.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.operaton.bpm.engine.impl.mock.MockExpressionManager

class OperatonEmbeddedProcessTestInitializer : ProcessTestInitializer {

  companion object {
    const val QUALIFIER = "operaton-embedded"
  }

  override fun qualifier() = QUALIFIER

  override fun initialize(request: ProcessTestInitializationRequest): OperatonEmbeddedProcessTestContext {
    val engine = object : StandaloneInMemProcessEngineConfiguration() {
      init {
        history = HISTORY_AUDIT
        databaseSchemaUpdate = DB_SCHEMA_UPDATE_TRUE
        jobExecutorActivate = false
        expressionManager = MockExpressionManager()
      }
    }.buildProcessEngine()
    val commandExecutor = EngineCommandExecutor { it.run() }
    val subscriptions = InMemSubscriptionRepository()
    val metadata = CachingProcessDefinitionMetaDataResolver(engine.repositoryService)
    val workerId = "process-test-${request.testClass.simpleName}"

    return OperatonEmbeddedProcessTestContext(
      processEngine = engine,
      runtimeApis = OperatonEmbeddedProcessTestRuntimeApis(
        startProcessApi = StartProcessApiImpl(engine.runtimeService, engine.repositoryService, commandExecutor, metadata),
        taskSubscriptionApi = OperatonTaskSubscriptionApiImpl(subscriptions),
        userTaskCompletionApi = OperatonUserTaskCompletionApiImpl(engine.taskService, subscriptions, commandExecutor),
        userTaskModificationApi = OperatonUserTaskModificationApiImpl(engine.taskService, commandExecutor),
        serviceTaskCompletionApi = OperatonServiceTaskCompletionApiImpl(
          workerId, engine.externalTaskService, subscriptions, LinearMemoryFailureRetrySupplier(3, 1), commandExecutor
        ),
        correlationApi = CorrelationApiImpl(engine.runtimeService, commandExecutor),
        signalApi = SignalApiImpl(engine.runtimeService, commandExecutor),
        deploymentApi = DeploymentApiImpl(engine.repositoryService, commandExecutor),
      ),
      metadata = metadata,
      workerId = workerId,
    )
  }
}
