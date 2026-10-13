package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.testing

import dev.bpmcrafters.processengineapi.testing.api.ProcessTestCapabilities
import org.operaton.bpm.engine.ProcessEngine
import org.operaton.bpm.engine.impl.util.ClockUtil
import java.time.Duration
import java.util.*

internal class OperatonEmbeddedProcessTestCapabilities(private val engine: ProcessEngine) : ProcessTestCapabilities {
  private var currentTime = Date()

  override fun supportsTimeTravel() = true

  override fun timePasses(duration: Duration) {
    currentTime = Date.from(currentTime.toInstant().plus(duration))
    ClockUtil.setCurrentTime(currentTime)
    engine.managementService.createJobQuery().executable().list()
      .forEach { engine.managementService.executeJob(it.id) }
  }
}
