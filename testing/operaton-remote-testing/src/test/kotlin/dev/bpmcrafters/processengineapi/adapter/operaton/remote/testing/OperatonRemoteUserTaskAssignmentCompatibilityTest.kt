package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import dev.bpmcrafters.processengineapi.testing.contract.fixture.UserTaskAssignmentFixture
import dev.bpmcrafters.processengineapi.testing.contract.suite.UserTaskAssignmentCompatibilitySuite

@UseProcessTestInitializer(OperatonRemoteProcessTestInitializer.QUALIFIER)
class OperatonRemoteUserTaskAssignmentCompatibilityTest : UserTaskAssignmentCompatibilitySuite() {
  override fun userTaskAssignmentFixture() = object : UserTaskAssignmentFixture {
    override fun classpathResource() = "contract/operaton/user-task-assignment.bpmn"
    override fun definitionKey() = "contract-user-task-assignment"
    override fun userTaskDescriptionKey() = "approval"
    override fun initialAssignee() = "gonzo"
    override fun assignee() = "kermit"
    override fun assigneeMetadataKey() = "assignee"
  }
}
