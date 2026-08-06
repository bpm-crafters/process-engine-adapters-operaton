package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties.*
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import org.springframework.boot.context.properties.bind.BindResult
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata


/**
 * Condition which returns true if `dev.bpm-crafters.process-api.adapter.operaton-embedded.enabled` is true
 */
open class OperatonEmbeddedAdapterEnabledCondition : Condition {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    // bind the value of "enabled" property
    val booleanBinderResult = Binder.get(context.environment)
      .bind("$DEFAULT_PREFIX.${OperatonEmbeddedAdapterProperties::enabled.name}", Boolean::class.java)
    if (booleanBinderResult.isBound) {
      return booleanBinderResult.get()
    }
    return false
  }
}

/**
 * Condition which returns true if the following conditions are true:
 * * `dev.bpm-crafters.process-api.adapter.operaton-embedded.enabled` is true
 * * `dev.bpm-crafters.process-api.adapter.operaton-embedded.user-tasks.execute-initial-pull-on-startup` is true
 */
open class OperatonEmbeddedAdapterUserTaskInitialPullEnabledCondition : OperatonEmbeddedAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<OperatonEmbeddedAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, OperatonEmbeddedAdapterProperties::class.java)
    if (propertiesBindResult.isBound) {
      return propertiesBindResult.get().userTasks.executeInitialPullOnStartup
    }
    return false
  }
}

/**
 * Condition which returns true if the following conditions are true:
 * * `dev.bpm-crafters.process-api.adapter.operaton-embedded.enabled` is true
 * * `dev.bpm-crafters.process-api.adapter.operaton-embedded.service-tasks.execute-initial-pull-on-startup` is true
 */
open class OperatonEmbeddedAdapterServiceTaskInitialPullEnabledCondition : OperatonEmbeddedAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<OperatonEmbeddedAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, OperatonEmbeddedAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      return propertiesBindResult.get().serviceTasks.executeInitialPullOnStartup
    }
    return false
  }
}

/**
 * Conditions matches if the given strategy is equal to the configured one in application property: `DEFAULT_PREFIX`.userTasks.deliveryStrategy
 */
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnUserTaskDeliveryStrategyCondition::class
)
annotation class ConditionalOnUserTaskDeliveryStrategy(
  val strategies: Array<UserTaskDeliveryStrategy> = [UserTaskDeliveryStrategy.EMBEDDED_SCHEDULED],
)

internal class OnUserTaskDeliveryStrategyCondition : OperatonEmbeddedAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {

    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<OperatonEmbeddedAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, OperatonEmbeddedAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val properties: OperatonEmbeddedAdapterProperties = propertiesBindResult.get()

      @Suppress("UNCHECKED_CAST")
      val strategies: Array<UserTaskDeliveryStrategy> = metadata
        .getAnnotationAttributes(ConditionalOnUserTaskDeliveryStrategy::class.java.name)
        ?.get(ConditionalOnUserTaskDeliveryStrategy::strategies.name) as Array<UserTaskDeliveryStrategy>

      return strategies.contains(properties.userTasks.deliveryStrategy)
    }

    return false
  }
}

/**
 * Conditions matches if the given strategy is equal to the configured one in application property: `DEFAULT_PREFIX`.serviceTasks.deliveryStrategy
 */
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnServiceTaskDeliveryStrategyCondition::class
)
annotation class ConditionalOnServiceTaskDeliveryStrategy(
  val strategy: ExternalServiceTaskDeliveryStrategy = ExternalServiceTaskDeliveryStrategy.EMBEDDED_SCHEDULED,
)

internal class OnServiceTaskDeliveryStrategyCondition : OperatonEmbeddedAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<OperatonEmbeddedAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, OperatonEmbeddedAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val properties: OperatonEmbeddedAdapterProperties = propertiesBindResult.get()

      val strategy = metadata
        .getAnnotationAttributes(ConditionalOnServiceTaskDeliveryStrategy::class.java.name)
        ?.get(ConditionalOnServiceTaskDeliveryStrategy::strategy.name) as ExternalServiceTaskDeliveryStrategy

      return properties.serviceTasks.deliveryStrategy == strategy
    }

    return false
  }
}
