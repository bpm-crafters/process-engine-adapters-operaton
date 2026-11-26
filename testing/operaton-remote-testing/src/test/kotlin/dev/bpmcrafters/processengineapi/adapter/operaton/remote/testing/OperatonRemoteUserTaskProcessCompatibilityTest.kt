package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import dev.bpmcrafters.processengineapi.testing.contract.fixture.UserTaskProcessFixture
import dev.bpmcrafters.processengineapi.testing.contract.suite.UserTaskProcessCompatibilitySuite

@UseProcessTestInitializer(OperatonRemoteProcessTestInitializer.QUALIFIER)
class OperatonRemoteUserTaskProcessCompatibilityTest : UserTaskProcessCompatibilitySuite() {
  override fun userTaskProcessFixture() = object : UserTaskProcessFixture {
    override fun classpathResource() = "contract/operaton/user-task.bpmn"
    override fun definitionKey() = "contract-user-task"
    override fun userTaskDescriptionKey() = "approval"
    override fun startElementId() = "start"
    override fun userTaskElementId() = "approval"
    override fun endElementId() = "end"
  }
}
