package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing

import dev.bpmcrafters.processengineapi.testing.api.ProcessTestCapabilities
import org.operaton.bpm.engine.ProcessEngine
import org.operaton.bpm.engine.impl.util.ClockUtil
import java.time.Duration
import java.util.Date

internal class OperatonEmbeddedProcessTestCapabilities(private val engine: ProcessEngine) : ProcessTestCapabilities {
  override fun supportsTimeTravel() = true

  override fun timePasses(duration: Duration) {
    ClockUtil.setCurrentTime(Date.from(ClockUtil.getCurrentTime().toInstant().plus(duration)))
    engine.managementService.createJobQuery().executable().list()
      .forEach { engine.managementService.executeJob(it.id) }
  }
}
