package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.shared.EngineCommandExecutor
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing.mockProcessDefinitionQuery
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing.processDefinitionFake
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing.processInstanceFake
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionAtElementCmd
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd
import dev.bpmcrafters.processengineapi.process.StartProcessByMessageAtElementCmd
import dev.bpmcrafters.processengineapi.process.StartProcessByMessageCmd
import org.operaton.bpm.engine.RepositoryService
import org.operaton.bpm.engine.RuntimeService
import org.operaton.bpm.engine.runtime.MessageCorrelationBuilder
import org.operaton.bpm.engine.runtime.ModificationBuilder
import org.operaton.bpm.engine.runtime.ProcessInstance
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class StartProcessApiImplTest {

  @Mock
  private lateinit var repositoryService: RepositoryService

  @Mock
  private lateinit var runtimeService: RuntimeService

  private lateinit var startProcessApi: StartProcessApiImpl

  @BeforeEach
  fun setUp() {
    startProcessApi = StartProcessApiImpl(
      runtimeService = runtimeService,
      repositoryService = repositoryService,
      commandExecutor = EngineCommandExecutor { it.run() }
    )
  }

  @Test
  fun `should start process via definition without payload`() {
    // given
    val startProcessByDefinitionCmd = StartProcessByDefinitionCmd("definitionKey", { emptyMap() })
    val processInstance: ProcessInstance = processInstanceFake(id = "someId")
    whenever(runtimeService.startProcessInstanceByKey(anyString(), anyOrNull(), anyMap())).thenReturn(processInstance)

    // when
    startProcessApi.startProcess(startProcessByDefinitionCmd).get()

    // then
    verify(runtimeService).startProcessInstanceByKey("definitionKey", null, emptyMap())
  }

  @Test
  fun `should start process via definition without payload with tenant`() {
    // given
    val startProcessByDefinitionCmd = StartProcessByDefinitionCmd(
      "definitionKey",
      { emptyMap() },
      mapOf(CommonRestrictions.TENANT_ID to "tenantId")
    )
    val processInstance = processInstanceFake(id = "someId")
    whenever(runtimeService.startProcessInstanceById(
      anyString(),
      anyOrNull(),
      anyMap(),
    )).thenReturn(processInstance)
    val processDefinition = processDefinitionFake(id = "definitionId", tenantId = "tenantId")
    val processDefinitionQuery = mockProcessDefinitionQuery(repositoryService)
    whenever(processDefinitionQuery.singleResult()).thenReturn(processDefinition)

    // when
    startProcessApi.startProcess(startProcessByDefinitionCmd).get()

    // then
    verify(runtimeService).startProcessInstanceById("definitionId", null, emptyMap())
  }

  @Test
  fun `should start process via definition with payload and business key`() {
    // given
    val processInstance: ProcessInstance = processInstanceFake(id = "someId")
    whenever(runtimeService.startProcessInstanceByKey(anyString(), anyOrNull(), anyMap())).thenReturn(processInstance)
    val startProcessByDefinitionCmd = StartProcessByDefinitionCmd("definitionKey", {
      mapOf(
        "key" to "value",
        CommonRestrictions.BUSINESS_KEY to "businessKey"
      )
    })

    // when
    startProcessApi.startProcess(startProcessByDefinitionCmd).get()

    // then
    verify(runtimeService).startProcessInstanceByKey(
      "definitionKey", "businessKey", mapOf(
        "key" to "value",
        CommonRestrictions.BUSINESS_KEY to "businessKey"
      )
    )
  }

  @Test
  fun `should start process via message with business key`() {
    // given
    val payload = mapOf(CommonRestrictions.BUSINESS_KEY to "testBusinessKey", "key" to "value")
    val startProcessByMessageCmd = StartProcessByMessageCmd("testMessage", { payload })
    val correlationBuilder = messageCorrelationMock()
    whenever(runtimeService.createMessageCorrelation(any())).thenReturn(correlationBuilder)

    // When
    startProcessApi.startProcess(startProcessByMessageCmd).get()

    // Then
    verify(runtimeService).createMessageCorrelation("testMessage")
    verify(correlationBuilder).processInstanceBusinessKey("testBusinessKey")
    verify(correlationBuilder).setVariables(mapOf(CommonRestrictions.BUSINESS_KEY to "testBusinessKey", "key" to "value"))
  }

  @Test
  fun `should start process via message with payload`() {
    // given
    val payload = mapOf("key" to "value")
    val startProcessByMessageCmd = StartProcessByMessageCmd("testMessage", { payload })
    val correlationBuilder = messageCorrelationMock()
    whenever(runtimeService.createMessageCorrelation(any())).thenReturn(correlationBuilder)

    // When
    startProcessApi.startProcess(startProcessByMessageCmd).get()

    // Then
    verify(runtimeService).createMessageCorrelation("testMessage")
    verify(correlationBuilder).setVariables(mapOf("key" to "value"))
    verify(correlationBuilder, times(0)).processInstanceBusinessKey(any())
  }

  @Test
  fun `should start process at element without payload`() {

    // given
    val businessKey = "myBusinessKey"
    val modificationBuilder = modifyProcessInstanceBuilderMock()
    val processDefinitionId = "simple-process:1:123"
    val processInstance = processInstanceMock(processDefinitionId)

    whenever(runtimeService.startProcessInstanceByKey(anyString(), anyOrNull(), anyMap())).thenReturn(processInstance)
    whenever(runtimeService.createModification(processDefinitionId)).thenReturn(modificationBuilder)

    val cmd = StartProcessByDefinitionAtElementCmd(
      definitionKey = "simple-process",
      elementId = "user-perform-task",
      payloadSupplier = { mapOf(CommonRestrictions.BUSINESS_KEY to businessKey) }
    )

    // when
    startProcessApi.startProcess(cmd).get()

    // then
    verify(runtimeService).startProcessInstanceByKey("simple-process", businessKey, mapOf("businessKey" to businessKey))
    verify(runtimeService).createModification(processDefinitionId)
    verify(modificationBuilder).startBeforeActivity("user-perform-task")
    verify(modificationBuilder).execute()
  }

  @Test
  fun `should start process at element via message without payload`() {

    // given
    val modificationBuilder = modifyProcessInstanceBuilderMock()
    val processDefinitionId = "simple-process:1:123"
    val processInstance = processInstanceMock(processDefinitionId)

    val correlationBuilder: MessageCorrelationBuilder = mock()
    whenever(correlationBuilder.setVariables(anyMap())).thenReturn(correlationBuilder)
    whenever(correlationBuilder.correlateStartMessage()).thenReturn(processInstance)
    whenever(runtimeService.createMessageCorrelation(any())).thenReturn(correlationBuilder)
    whenever(runtimeService.createModification(processDefinitionId)).thenReturn(modificationBuilder)

    val cmd = StartProcessByMessageAtElementCmd(
      messageName = "startMessage",
      elementId = "user-perform-task",
      payloadSupplier = { emptyMap() }
    )

    // when
    startProcessApi.startProcess(cmd).get()

    // then
    verify(runtimeService).createMessageCorrelation("startMessage")
    verify(correlationBuilder).setVariables(emptyMap())
    verify(runtimeService).createModification(processDefinitionId)
    verify(modificationBuilder).processInstanceIds("instance-123")
    verify(modificationBuilder).startBeforeActivity("user-perform-task")
    verify(modificationBuilder).execute()
  }

  private fun processInstanceMock(processDefinitionId: String): ProcessInstance =
    processInstanceFake(id = "instance-123", processDefinitionId = processDefinitionId)

  private fun messageCorrelationMock(): MessageCorrelationBuilder {
    val startedInstance = processInstanceFake(id = "someId")
    val builder: MessageCorrelationBuilder = mock()
    lenient().whenever(builder.processInstanceBusinessKey(any())).thenReturn(builder)
    whenever(builder.setVariables(anyMap())).thenReturn(builder)
    whenever(builder.correlateStartMessage()).thenReturn(startedInstance)

    return builder
  }

  private fun modifyProcessInstanceBuilderMock(): ModificationBuilder {
    val builder = mock<ModificationBuilder>()

    whenever(builder.processInstanceIds(any<String>())).thenReturn(builder)
    whenever(builder.startBeforeActivity(any())).thenReturn(builder)
    doNothing().`when`(builder).execute()

    return builder
  }
}