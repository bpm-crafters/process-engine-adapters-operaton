package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.shared.EngineCommandExecutor
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.TaskService
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

/**
 * Implementation using Operaton taskService Java API for completion of user tasks.
 * @since 0.0.1
 */
class OperatonUserTaskCompletionApiImpl(
  private val taskService: TaskService,
  private val subscriptionRepository: SubscriptionRepository,
  private val commandExecutor: EngineCommandExecutor
) : UserTaskCompletionApi {

  override fun completeTask(cmd: CompleteTaskCmd): CompletableFuture<Empty> {
    logger.debug { "PROCESS-ENGINE-OPERATON-EMBEDDED-011: completing user task ${cmd.taskId}." }
    return commandExecutor.execute {
      taskService.complete(
        cmd.taskId,
        cmd.get()
      )
      subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
        termination.accept(TaskInformation(cmd.taskId, emptyMap()).withReason(TaskInformation.COMPLETE))
        logger.debug { "PROCESS-ENGINE-OPERATON-EMBEDDED-012: successfully completed user task ${cmd.taskId}." }
      }
      Empty
    }
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): CompletableFuture<Empty> {
    logger.debug { "PROCESS-ENGINE-OPERATON-EMBEDDED-013: throwing error on user task ${cmd.taskId}." }
    return commandExecutor.execute {
      taskService.handleBpmnError(
        cmd.taskId,
        cmd.errorCode
      )
      subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
        termination.accept(TaskInformation(cmd.taskId, emptyMap()).withReason(TaskInformation.COMPLETE))
        logger.debug { "PROCESS-ENGINE-OPERATON-EMBEDDED-014: successfully thrown error on user task ${cmd.taskId}." }
      }
      Empty
    }
  }
}
