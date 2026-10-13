package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestAssertApi
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestQueryApi
import dev.bpmcrafters.processengineapi.testing.config.ProcessTestPollingConfiguration
import org.awaitility.Awaitility.await
import org.camunda.community.rest.client.api.HistoryApiClient
import org.camunda.community.rest.client.api.IncidentApiClient
import org.camunda.community.rest.client.api.ProcessInstanceApiClient
import org.camunda.community.rest.client.model.ActivityInstanceDto
import org.camunda.community.rest.client.model.HistoricActivityInstanceQueryDto
import org.camunda.community.rest.client.model.HistoricActivityInstanceQueryDtoSortingInner
import org.camunda.community.rest.client.model.HistoricActivityInstanceQueryDtoSortingInner.SortByEnum
import org.camunda.community.rest.client.model.HistoricActivityInstanceQueryDtoSortingInner.SortOrderEnum
import org.camunda.community.rest.client.model.HistoricProcessInstanceQueryDto
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import java.util.concurrent.Callable
import java.util.function.Predicate

private const val MAX_HISTORIC_ACTIVITIES = 1_000

internal class OperatonRemoteProcessTestAssertApi(
  application: ConfigurableApplicationContext,
  private val query: ProcessTestQueryApi,
  private val polling: ProcessTestPollingConfiguration
) : ProcessTestAssertApi {
  private val processes = application.getBean<ProcessInstanceApiClient>()
  private val history = application.getBean<HistoryApiClient>()
  private val incidents = application.getBean<IncidentApiClient>()

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

  override fun processWaitsInElement(instanceId: String, elementId: String, restrictions: Map<String, String>) {
    eventually {
      require(processes.getActivityInstanceTree(instanceId).body.contains(elementId)) {
        "Process $instanceId is not active in $elementId"
      }
    }
  }

  override fun processHasPassed(instanceId: String, restrictions: Map<String, String>, vararg elementIds: String) {
    val passedElementIds = passedElementIds(instanceId, elementIds)
    require(elementIds.all { it in passedElementIds }) {
      "Expected ${elementIds.toList()} to be passed; actual: $passedElementIds"
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
      "Expected ${elementIds.toList()} not to be passed; actual: $passedElementIds"
    }
  }

  override fun processIsFinished(instanceId: String, restrictions: Map<String, String>) {
    eventually {
      require(query.getProcessInformation(instanceId) == null) { "Process $instanceId is still active" }
      require(
        history.queryHistoricProcessInstances(0, 1, HistoricProcessInstanceQueryDto().processInstanceId(instanceId))
          .body.orEmpty().isNotEmpty()
      ) { "No historic process instance found for $instanceId" }
    }
  }

  override fun processHasIncidents(instanceId: String, restrictions: Map<String, String>) {
    eventually {
      require(
        incidents.getIncidents(
          null, instanceId, null, null, null, null, null, null, null, null,
          null, null, null, null, null, null, null, null, null, 0, 1
        ).body.orEmpty().isNotEmpty()
      ) { "No incidents found" }
    }
  }

  private fun passedElementIds(instanceId: String, expectedElementIds: Array<out String>): List<String> {
    require(expectedElementIds.isNotEmpty()) { "At least one element id is required." }
    val activitiesInExecutionOrder = HistoricActivityInstanceQueryDto()
      .processInstanceId(instanceId)
      .addSortingItem(ascendingBy(SortByEnum.START_TIME))
      .addSortingItem(ascendingBy(SortByEnum.OCCURRENCE))
    return history.queryHistoricActivityInstances(0, MAX_HISTORIC_ACTIVITIES, activitiesInExecutionOrder).body.orEmpty()
      .mapNotNull { it.activityId }
  }

  private fun ascendingBy(sortBy: SortByEnum) =
    HistoricActivityInstanceQueryDtoSortingInner().sortBy(sortBy).sortOrder(SortOrderEnum.ASC)

  private fun <T> eventually(supplier: () -> T): T =
    await()
      .pollInterval(polling.pollInterval)
      .atMost(polling.timeout)
      .until(Callable { supplier() }, Predicate { true })

  private fun selected(tasks: List<TaskInformation>): TaskInformation =
    requireNotNull(tasks.singleOrNull()) { "Expected exactly one task but found ${tasks.size}: $tasks" }
}

private fun ActivityInstanceDto?.contains(elementId: String): Boolean =
  this != null && (activityId == elementId || childActivityInstances.orEmpty().any { it.contains(elementId) })
