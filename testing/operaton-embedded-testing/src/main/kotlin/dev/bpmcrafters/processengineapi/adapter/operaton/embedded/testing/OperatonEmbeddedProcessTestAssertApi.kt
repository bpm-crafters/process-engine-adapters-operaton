package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestAssertApi
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestQueryApi
import dev.bpmcrafters.processengineapi.testing.config.ProcessTestPollingConfiguration
import org.awaitility.Awaitility.await
import org.operaton.bpm.engine.ProcessEngine
import java.util.concurrent.Callable
import java.util.function.Predicate

internal class OperatonEmbeddedProcessTestAssertApi(
  private val engine: ProcessEngine,
  private val query: ProcessTestQueryApi,
  private val polling: ProcessTestPollingConfiguration
) : ProcessTestAssertApi {

  override fun getSupportedRestrictions() = query.getSupportedRestrictions()

  override fun processWaitsInUserTask(
    instanceId: String,
    taskDescriptionKey: String?,
    restrictions: Map<String, String>,
    predicate: Predicate<TaskInformation>?
  ) = eventually {
    selected(
      query.findUserTasks(
        taskDescriptionKey,
        restrictions + (CommonRestrictions.PROCESS_INSTANCE_ID to instanceId),
        predicate
      )
    )
  }

  override fun processWaitsInElement(instanceId: String, elementId: String, restrictions: Map<String, String>) {
    eventually {
      val activeElementIds = engine.runtimeService.getActiveActivityIds(instanceId)
      require(activeElementIds.contains(elementId)) { "Active elements: $activeElementIds" }
    }
  }

  override fun userTaskIsSelected(
    taskDescriptionKey: String?,
    restrictions: Map<String, String>,
    predicate: Predicate<TaskInformation>?
  ) = eventually { selected(query.findUserTasks(taskDescriptionKey, restrictions, predicate)) }

  override fun externalTaskIsSelected(
    taskDescriptionKey: String?,
    restrictions: Map<String, String>,
    predicate: Predicate<TaskInformation>?
  ) = eventually { selected(query.findExternalTasks(taskDescriptionKey, restrictions, predicate)) }

  override fun processHasPassed(instanceId: String, restrictions: Map<String, String>, vararg elementIds: String) {
    val passedElementIds = passedElementIds(instanceId, elementIds)
    require(elementIds.all { it in passedElementIds }) {
      "Expected ${elementIds.toList()} to be passed; actual finished elements: $passedElementIds"
    }
  }

  override fun processHasPassedInOrder(instanceId: String, restrictions: Map<String, String>, vararg elementIds: String) {
    val passedElementIds = passedElementIds(instanceId, elementIds)
    elementIds.fold(0) { searchFrom, elementId ->
      val offset = passedElementIds.subList(searchFrom, passedElementIds.size).indexOf(elementId)
      require(offset >= 0) { "Expected ${elementIds.toList()} in order; actual: $passedElementIds" }
      searchFrom + offset + 1
    }
  }

  override fun processHasNotPassed(instanceId: String, restrictions: Map<String, String>, vararg elementIds: String) {
    val passedElementIds = passedElementIds(instanceId, elementIds)
    require(elementIds.none { it in passedElementIds }) {
      "Expected ${elementIds.toList()} not to be passed; actual finished elements: $passedElementIds"
    }
  }

  override fun processIsFinished(instanceId: String, restrictions: Map<String, String>) {
    eventually {
      require(
        engine.runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult() == null
      ) { "Process is still active" }
      require(
        engine.historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult() != null
      ) { "No historic process instance found" }
    }
  }

  override fun processHasIncidents(instanceId: String, restrictions: Map<String, String>) {
    eventually {
      require(
        engine.runtimeService.createIncidentQuery().processInstanceId(instanceId).count() > 0
      ) { "No incidents found" }
    }
  }

  private fun passedElementIds(instanceId: String, expectedElementIds: Array<out String>): List<String> {
    require(expectedElementIds.isNotEmpty()) { "At least one element id is required." }
    return engine.historyService.createHistoricActivityInstanceQuery()
      .processInstanceId(instanceId)
      .orderByHistoricActivityInstanceStartTime().asc()
      .orderPartiallyByOccurrence().asc()
      .list()
      .map { it.activityId }
  }

  private fun <T> eventually(supplier: () -> T): T =
    await()
      .pollInterval(polling.pollInterval)
      .atMost(polling.timeout)
      .ignoreExceptionsInstanceOf(IllegalArgumentException::class.java)
      .until(Callable { supplier() }, Predicate { true })

  private fun selected(tasks: List<TaskInformation>): TaskInformation {
    require(tasks.size == 1) { "Expected exactly one task but found ${tasks.size}: $tasks" }
    return tasks.single()
  }
}
