package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import dev.bpmcrafters.processengineapi.testing.contract.fixture.ExternalTaskProcessFixture
import dev.bpmcrafters.processengineapi.testing.contract.suite.ExternalTaskCompatibilitySuite

@UseProcessTestInitializer(OperatonRemoteProcessTestInitializer.QUALIFIER)
class OperatonRemoteExternalTaskCompatibilityTest : ExternalTaskCompatibilitySuite() {
  override fun externalTaskProcessFixture() = object : ExternalTaskProcessFixture {
    override fun classpathResource() = "contract/operaton/external-task.bpmn"
    override fun definitionKey() = "contract-external-task"
    override fun externalTaskDescriptionKey() = "contract-worker"
    override fun endElementId() = "end"
  }
}
