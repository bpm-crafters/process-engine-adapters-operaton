package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterProperties.UserTaskDeliveryStrategy
import org.springframework.boot.context.properties.bind.BindResult
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * Condition which returns true if `dev.bpm-crafters.process-api.adapter.operaton-remote.enabled` is true
 */
open class OperatonRemoteAdapterEnabledCondition : Condition {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    // bind the value of "enabled" property
    val booleanBinderResult = Binder.get(context.environment).bind("$DEFAULT_PREFIX.${OperatonRemoteAdapterProperties::enabled.name}", Boolean::class.java)
    if (booleanBinderResult.isBound) {
      return booleanBinderResult.get()
    }
    return false
  }
}

/**
 * Conditions matches if the given strategy is equal to the configured one in application property: `DEFAULT_PREFIX`.userTasks.deliveryStrategy
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnUserTaskDeliveryStrategyCondition::class
)
annotation class ConditionalOnUserTaskDeliveryStrategy(
  val strategy: UserTaskDeliveryStrategy = UserTaskDeliveryStrategy.REMOTE_SCHEDULED,
)

internal class OnUserTaskDeliveryStrategyCondition : OperatonRemoteAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {

    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<OperatonRemoteAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, OperatonRemoteAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val properties: OperatonRemoteAdapterProperties = propertiesBindResult.get()

      val strategy = metadata
        .getAnnotationAttributes(ConditionalOnUserTaskDeliveryStrategy::class.java.name)
        ?.get(ConditionalOnUserTaskDeliveryStrategy::strategy.name) as UserTaskDeliveryStrategy

      return properties.userTasks.deliveryStrategy == strategy
    }

    return false
  }
}

/**
 * Conditions matches if the given strategy is equal to the configured one in application property: `DEFAULT_PREFIX`.serviceTasks.deliveryStrategy
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnServiceTaskDeliveryStrategyCondition::class
)
annotation class ConditionalOnServiceTaskDeliveryStrategy(
  val strategy: ExternalServiceTaskDeliveryStrategy = ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED,
)

internal class OnServiceTaskDeliveryStrategyCondition : OperatonRemoteAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<OperatonRemoteAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, OperatonRemoteAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val properties: OperatonRemoteAdapterProperties = propertiesBindResult.get()

      val strategy = metadata
        .getAnnotationAttributes(ConditionalOnServiceTaskDeliveryStrategy::class.java.name)
        ?.get(ConditionalOnServiceTaskDeliveryStrategy::strategy.name) as ExternalServiceTaskDeliveryStrategy

      return properties.serviceTasks.deliveryStrategy == strategy
    }

    return false
  }
}
