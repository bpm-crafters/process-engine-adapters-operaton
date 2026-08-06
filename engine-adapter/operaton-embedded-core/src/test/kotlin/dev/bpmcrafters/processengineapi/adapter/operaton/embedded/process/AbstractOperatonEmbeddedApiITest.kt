package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.process

import dev.bpmcrafters.processengineapi.test.JGivenBaseIntegrationTest
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.operaton.bpm.engine.ProcessEngine
import org.operaton.bpm.engine.ProcessEngineConfiguration
import org.operaton.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.operaton.bpm.engine.impl.mock.MockExpressionManager
import org.junit.jupiter.api.AfterEach

abstract class AbstractOperatonEmbeddedApiITest(override val processTestHelper: ProcessTestHelper) : JGivenBaseIntegrationTest(processTestHelper) {

  companion object {
    const val KEY = "simple-process"
    const val START_MESSAGE = "startMessage"
    const val BPMN = "bpmn/$KEY.bpmn"

    const val USER_TASK = "user-perform-task"
    const val EXTERNAL_TASK = "execute-action-external"

    // Must stay non-final: Operaton's ProcessEngineExtension reflectively injects ProcessEngine fields.
    var processEngine: ProcessEngine = object : StandaloneInMemProcessEngineConfiguration() {
      init {
        history = HISTORY_AUDIT
        databaseSchemaUpdate = DB_SCHEMA_UPDATE_TRUE
        jobExecutorActivate = false
        expressionManager = MockExpressionManager()
      }
    }.buildProcessEngine()
  }

  @AfterEach
  fun tearDown() {
    processTestHelper.clearAllSubscriptions()
  }

}
