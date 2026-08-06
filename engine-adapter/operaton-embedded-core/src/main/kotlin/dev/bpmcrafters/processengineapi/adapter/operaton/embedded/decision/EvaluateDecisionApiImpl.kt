package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.decision

import dev.bpmcrafters.processengineapi.adapter.operaton.common.serialization.AdapterDataConverter
import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.shared.EngineCommandExecutor
import dev.bpmcrafters.processengineapi.decision.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.dmn.engine.DmnDecisionResult
import org.operaton.bpm.dmn.engine.DmnDecisionResultEntries
import org.operaton.bpm.engine.DecisionService
import org.operaton.bpm.engine.dmn.DecisionsEvaluationBuilder
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

class EvaluateDecisionApiImpl(
  private val decisionService: DecisionService,
  private val dataConverter: AdapterDataConverter,
  private val commandExecutor: EngineCommandExecutor
) : EvaluateDecisionApi {

  override fun evaluateDecision(command: DecisionEvaluationCommand): CompletableFuture<DecisionEvaluationResult> {
    return when (command) {
      is DecisionByRefEvaluationCommand -> {
        logger.debug {
          "PROCESS-ENGINE-OPERATON-EMBEDDED-061: Evaluating decision by reference ${command.decisionRef}"
        }
        commandExecutor.execute {
          val result = decisionService
            .evaluateDecisionByKey(command.decisionRef)
            .applyRestrictions(ensureSupported(command.restrictionSupplier.get()))
            .variables(command.payloadSupplier.get())
            .evaluate()
          if (result.size == 0) {
            NoDecisionResult
          } else {
            DelegatingDmnDecisionResult(result, dataConverter)
          }
        }
      }

      else -> throw UnsupportedOperationException("Evaluate Decision command of type ${command::class.qualifiedName} is not implemented yet")
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(
    CommonRestrictions.TENANT_ID,
    CommonRestrictions.WITHOUT_TENANT_ID,
  )

  fun DecisionsEvaluationBuilder.applyRestrictions(restrictions: Map<String, String>): DecisionsEvaluationBuilder = this.apply {
    restrictions
      .forEach { (key, value) ->
        when (key) {
          CommonRestrictions.TENANT_ID -> this.decisionDefinitionTenantId(value).apply {
            require(!restrictions.containsKey(CommonRestrictions.WITHOUT_TENANT_ID)) {
              "Illegal restriction combination. ${CommonRestrictions.TENANT_ID} " +
                "and ${CommonRestrictions.WITHOUT_TENANT_ID} can't be provided in the same time because they are mutually exclusive."
            }
          }

          CommonRestrictions.WITHOUT_TENANT_ID -> this.decisionDefinitionWithoutTenantId().apply {
            require(!restrictions.containsKey(CommonRestrictions.TENANT_ID)) {
              "Illegal restriction combination. ${CommonRestrictions.TENANT_ID} " +
                "and ${CommonRestrictions.WITHOUT_TENANT_ID} can't be provided in the same time because they are mutually exclusive."
            }
          }
        }
      }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }
}

