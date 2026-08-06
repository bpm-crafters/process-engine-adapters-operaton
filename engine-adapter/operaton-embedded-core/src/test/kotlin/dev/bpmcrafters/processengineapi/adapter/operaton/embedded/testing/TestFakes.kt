package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing

import org.mockito.Mockito
import org.mockito.Mockito.withSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.operaton.bpm.engine.RepositoryService
import org.operaton.bpm.engine.RuntimeService
import org.operaton.bpm.engine.TaskService
import org.operaton.bpm.engine.delegate.DelegateTask
import org.operaton.bpm.engine.repository.ProcessDefinition
import org.operaton.bpm.engine.repository.ProcessDefinitionQuery
import org.operaton.bpm.engine.runtime.MessageCorrelationBuilder
import org.operaton.bpm.engine.runtime.ProcessInstance
import org.operaton.bpm.engine.task.IdentityLink
import org.operaton.bpm.engine.task.Task
import org.operaton.bpm.engine.task.TaskQuery
import java.util.*

/*
 * Small mock factories replacing the discontinued io.holunda.c7:c7-mockito fakes.
 * All mocks are lenient, so unused stubbings never trip strict-stub checking.
 */

private inline fun <reified T : Any> lenientMock(): T =
  Mockito.mock(T::class.java, withSettings().strictness(Strictness.LENIENT))

private inline fun <reified T : Any> fluentMock(): T =
  Mockito.mock(T::class.java, withSettings().strictness(Strictness.LENIENT).defaultAnswer(Mockito.RETURNS_SELF))

fun taskFake(
  id: String? = null,
  processDefinitionId: String? = null,
  processInstanceId: String? = null,
  tenantId: String? = null,
  taskDefinitionKey: String? = null,
  name: String? = null,
  description: String? = null,
  assignee: String? = null,
  createTime: Date? = null,
  followUpDate: Date? = null,
  dueDate: Date? = null,
  formKey: String? = null,
  lastUpdated: Date? = null
): Task = lenientMock<Task>().also {
  whenever(it.id).thenReturn(id)
  whenever(it.processDefinitionId).thenReturn(processDefinitionId)
  whenever(it.processInstanceId).thenReturn(processInstanceId)
  whenever(it.tenantId).thenReturn(tenantId)
  whenever(it.taskDefinitionKey).thenReturn(taskDefinitionKey)
  whenever(it.name).thenReturn(name)
  whenever(it.description).thenReturn(description)
  whenever(it.assignee).thenReturn(assignee)
  whenever(it.createTime).thenReturn(createTime)
  whenever(it.followUpDate).thenReturn(followUpDate)
  whenever(it.dueDate).thenReturn(dueDate)
  whenever(it.formKey).thenReturn(formKey)
  whenever(it.lastUpdated).thenReturn(lastUpdated)
}

fun delegateTaskFake(
  id: String? = null,
  processDefinitionId: String? = null,
  processInstanceId: String? = null,
  tenantId: String? = null,
  taskDefinitionKey: String? = null,
  name: String? = null,
  description: String? = null,
  assignee: String? = null,
  createTime: Date? = null,
  followUpDate: Date? = null,
  dueDate: Date? = null,
  lastUpdated: Date? = null,
  candidates: Set<IdentityLink> = emptySet(),
  variables: Map<String, Any> = emptyMap()
): DelegateTask = lenientMock<DelegateTask>().also {
  whenever(it.id).thenReturn(id)
  whenever(it.processDefinitionId).thenReturn(processDefinitionId)
  whenever(it.processInstanceId).thenReturn(processInstanceId)
  whenever(it.tenantId).thenReturn(tenantId)
  whenever(it.taskDefinitionKey).thenReturn(taskDefinitionKey)
  whenever(it.name).thenReturn(name)
  whenever(it.description).thenReturn(description)
  whenever(it.assignee).thenReturn(assignee)
  whenever(it.createTime).thenReturn(createTime)
  whenever(it.followUpDate).thenReturn(followUpDate)
  whenever(it.dueDate).thenReturn(dueDate)
  whenever(it.lastUpdated).thenReturn(lastUpdated)
  whenever(it.candidates).thenReturn(candidates)
  whenever(it.variables).thenReturn(variables)
}

fun processInstanceFake(id: String, processDefinitionId: String? = null): ProcessInstance =
  lenientMock<ProcessInstance>().also {
    whenever(it.id).thenReturn(id)
    whenever(it.processDefinitionId).thenReturn(processDefinitionId)
  }

fun processDefinitionFake(id: String, tenantId: String? = null): ProcessDefinition =
  lenientMock<ProcessDefinition>().also {
    whenever(it.id).thenReturn(id)
    whenever(it.tenantId).thenReturn(tenantId)
  }

/**
 * Returns a fluent [ProcessDefinitionQuery] mock and stubs it into the repository service.
 */
fun mockProcessDefinitionQuery(repositoryService: RepositoryService): ProcessDefinitionQuery =
  fluentMock<ProcessDefinitionQuery>().also {
    whenever(repositoryService.createProcessDefinitionQuery()).thenReturn(it)
  }

/**
 * Returns a fluent [TaskQuery] mock and stubs it into the task service.
 */
fun mockTaskQuery(taskService: TaskService): TaskQuery =
  fluentMock<TaskQuery>().also {
    whenever(taskService.createTaskQuery()).thenReturn(it)
  }

/**
 * Returns a fluent [MessageCorrelationBuilder] mock and stubs it into the runtime service
 * for the given message name.
 */
fun mockMessageCorrelation(runtimeService: RuntimeService, messageName: String): MessageCorrelationBuilder =
  fluentMock<MessageCorrelationBuilder>().also {
    whenever(runtimeService.createMessageCorrelation(messageName)).thenReturn(it)
  }
