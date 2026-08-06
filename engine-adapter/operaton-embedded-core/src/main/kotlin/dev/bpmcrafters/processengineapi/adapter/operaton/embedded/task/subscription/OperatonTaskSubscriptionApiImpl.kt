package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.subscription

import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.impl.task.AbstractTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository

/**
 * Operaton task subscription implementation.
 */
class OperatonTaskSubscriptionApiImpl(
  subscriptionRepository: SubscriptionRepository
) : AbstractTaskSubscriptionApiImpl(
  subscriptionRepository = subscriptionRepository
) {

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }
}
