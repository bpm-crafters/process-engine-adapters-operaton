package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import dev.bpmcrafters.processengineapi.testing.contract.fixture.AlternateStartFixture
import dev.bpmcrafters.processengineapi.testing.contract.suite.AlternateStartCompatibilitySuite

@UseProcessTestInitializer(OperatonRemoteProcessTestInitializer.QUALIFIER)
class OperatonRemoteAlternateStartCompatibilityTest : AlternateStartCompatibilitySuite() {
  override fun alternateStartFixture() = object : AlternateStartFixture {
    override fun classpathResource() = "contract/operaton/alternate-starts.bpmn"
    override fun definitionKey() = "contract-alternate-starts"
    override fun startMessageName() = "contract.start"
    override fun entryElementId() = "approval"
    override fun userTaskDescriptionKey() = "approval"
  }
}
