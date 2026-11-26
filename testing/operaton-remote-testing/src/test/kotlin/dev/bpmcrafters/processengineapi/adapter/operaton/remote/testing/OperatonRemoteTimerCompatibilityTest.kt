package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import dev.bpmcrafters.processengineapi.testing.contract.fixture.TimerProcessFixture
import dev.bpmcrafters.processengineapi.testing.contract.suite.TimerCompatibilitySuite
import org.junit.jupiter.api.Disabled

@Disabled("Operaton REST does not expose controllable engine time.")
@UseProcessTestInitializer(OperatonRemoteProcessTestInitializer.QUALIFIER)
class OperatonRemoteTimerCompatibilityTest : TimerCompatibilitySuite() {
  override fun timerProcessFixture() = object : TimerProcessFixture {
    override fun classpathResource() = "contract/operaton/timer.bpmn"
    override fun definitionKey() = "contract-timer"
    override fun timerElementId() = "timerWait"
    override fun userTaskDescriptionKey() = "approval"
  }
}
