package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.testing.api.ProcessTestCapabilities
import java.time.Duration

internal class OperatonRemoteProcessTestCapabilities : ProcessTestCapabilities {

  override fun supportsTimeTravel() = false

  override fun timePasses(duration: Duration): Nothing =
    throw UnsupportedOperationException("Remote Operaton engines do not support test time travel.")
}
