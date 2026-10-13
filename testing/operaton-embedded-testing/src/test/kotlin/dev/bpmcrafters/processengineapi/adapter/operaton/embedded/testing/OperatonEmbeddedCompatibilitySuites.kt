package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing

import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import dev.bpmcrafters.processengineapi.testing.contract.fixture.*
import dev.bpmcrafters.processengineapi.testing.contract.suite.*

@UseProcessTestInitializer(OperatonEmbeddedProcessTestInitializer.QUALIFIER)
class OperatonEmbeddedUserTaskProcessCompatibilityTest : UserTaskProcessCompatibilitySuite() {
  override fun userTaskProcessFixture() = object : UserTaskProcessFixture {
    override fun classpathResource() = "contract/operaton/user-task.bpmn"
    override fun definitionKey() = "contract-user-task"
    override fun userTaskDescriptionKey() = "approval"
    override fun startElementId() = "start"
    override fun userTaskElementId() = "approval"
    override fun endElementId() = "end"
  }
}

@UseProcessTestInitializer(OperatonEmbeddedProcessTestInitializer.QUALIFIER)
class OperatonEmbeddedUserTaskAssignmentCompatibilityTest : UserTaskAssignmentCompatibilitySuite() {
  override fun userTaskAssignmentFixture() = object : UserTaskAssignmentFixture {
    override fun classpathResource() = "contract/operaton/user-task-assignment.bpmn"
    override fun definitionKey() = "contract-user-task-assignment"
    override fun userTaskDescriptionKey() = "approval"
    override fun initialAssignee() = "gonzo"
    override fun assignee() = "kermit"
    override fun assigneeMetadataKey() = "assignee"
  }
}

@UseProcessTestInitializer(OperatonEmbeddedProcessTestInitializer.QUALIFIER)
class OperatonEmbeddedExternalTaskCompatibilityTest : ExternalTaskCompatibilitySuite() {
  override fun externalTaskProcessFixture() = object : ExternalTaskProcessFixture {
    override fun classpathResource() = "contract/operaton/external-task.bpmn"
    override fun definitionKey() = "contract-external-task"
    override fun externalTaskDescriptionKey() = "contract-worker"
    override fun endElementId() = "end"
  }
}

@UseProcessTestInitializer(OperatonEmbeddedProcessTestInitializer.QUALIFIER)
class OperatonEmbeddedMessageAndSignalCompatibilityTest : MessageAndSignalCompatibilitySuite() {
  override fun messageAndSignalFixture() = object : MessageAndSignalFixture {
    override fun classpathResource() = "contract/operaton/message-and-signal.bpmn"
    override fun definitionKey() = "contract-message-and-signal"
    override fun messageName() = "contract.continue"
    override fun correlationVariable() = "correlationKey"
    override fun correlationKey() = "customer-42"
    override fun signalName() = "contract.continue.signal"
    override fun messageCatchElementId() = "messageWait"
    override fun signalCatchElementId() = "signalWait"
    override fun endElementId() = "end"
  }
}

@UseProcessTestInitializer(OperatonEmbeddedProcessTestInitializer.QUALIFIER)
class OperatonEmbeddedTimerCompatibilityTest : TimerCompatibilitySuite() {
  override fun timerProcessFixture() = object : TimerProcessFixture {
    override fun classpathResource() = "contract/operaton/timer.bpmn"
    override fun definitionKey() = "contract-timer"
    override fun timerElementId() = "timerWait"
    override fun userTaskDescriptionKey() = "approval"
  }
}

@UseProcessTestInitializer(OperatonEmbeddedProcessTestInitializer.QUALIFIER)
class OperatonEmbeddedAlternateStartCompatibilityTest : AlternateStartCompatibilitySuite() {
  override fun alternateStartFixture() = object : AlternateStartFixture {
    override fun classpathResource() = "contract/operaton/alternate-starts.bpmn"
    override fun definitionKey() = "contract-alternate-starts"
    override fun startMessageName() = "contract.start"
    override fun entryElementId() = "approval"
    override fun userTaskDescriptionKey() = "approval"
  }
}
