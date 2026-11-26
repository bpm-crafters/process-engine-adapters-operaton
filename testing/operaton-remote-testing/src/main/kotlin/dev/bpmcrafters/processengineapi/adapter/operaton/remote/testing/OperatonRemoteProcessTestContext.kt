package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.testing.api.*
import dev.bpmcrafters.processengineapi.testing.config.ProcessTestPollingConfiguration
import org.springframework.context.ConfigurableApplicationContext
import java.time.Duration

class OperatonRemoteProcessTestContext(
  private val application: ConfigurableApplicationContext,
  private val stopHandler: () -> Unit,
  private val runtimeApis: OperatonRemoteProcessTestRuntimeApis,
  workerId: String
) : ProcessTestContext {
  private val queryApi = OperatonRemoteProcessTestQueryApi(application, workerId)
  private val polling = ProcessTestPollingConfiguration(Duration.ofMillis(100), Duration.ofSeconds(10))

  override fun qualifier() = OperatonRemoteProcessTestInitializer.QUALIFIER
  override fun runtime() = runtimeApis
  override fun query(): ProcessTestQueryApi = queryApi
  override fun assertions(): ProcessTestAssertApi = OperatonRemoteProcessTestAssertApi(application, queryApi, polling)
  override fun payloads(): ProcessTestPayloadApi = OperatonRemoteProcessTestPayloadApi(application)
  override fun capabilities(): ProcessTestCapabilities = OperatonRemoteProcessTestCapabilities()
  override fun diagnostics() = ProcessTestDiagnostics(polling)

  override fun close() {
    application.close()
    stopHandler.invoke()
  }
}
