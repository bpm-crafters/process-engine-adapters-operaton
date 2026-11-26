package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process.ProcessDefinitionMetaDataResolver
import dev.bpmcrafters.processengineapi.testing.api.*
import dev.bpmcrafters.processengineapi.testing.config.ProcessTestPollingConfiguration
import org.operaton.bpm.engine.ProcessEngine
import org.operaton.bpm.engine.impl.util.ClockUtil
import java.time.Duration

class OperatonEmbeddedProcessTestContext(
  private val processEngine: ProcessEngine,
  private val runtimeApis: OperatonEmbeddedProcessTestRuntimeApis,
  metadata: ProcessDefinitionMetaDataResolver,
  workerId: String,
) : ProcessTestContext {
  private val queryApi = OperatonEmbeddedProcessTestQueryApi(processEngine, metadata, workerId)
  private val payloadApi = OperatonEmbeddedProcessTestPayloadApi(processEngine)
  private val polling = ProcessTestPollingConfiguration(Duration.ofMillis(25), Duration.ofSeconds(5))

  override fun qualifier() = OperatonEmbeddedProcessTestInitializer.QUALIFIER
  override fun runtime() = runtimeApis
  override fun query(): ProcessTestQueryApi = queryApi
  override fun assertions(): ProcessTestAssertApi = OperatonEmbeddedProcessTestAssertApi(processEngine, queryApi, polling)
  override fun payloads(): ProcessTestPayloadApi = payloadApi
  override fun capabilities(): ProcessTestCapabilities = OperatonEmbeddedProcessTestCapabilities(processEngine)
  override fun diagnostics() = ProcessTestDiagnostics(polling)

  override fun close() {
    ClockUtil.reset()
    processEngine.close()
  }
}
