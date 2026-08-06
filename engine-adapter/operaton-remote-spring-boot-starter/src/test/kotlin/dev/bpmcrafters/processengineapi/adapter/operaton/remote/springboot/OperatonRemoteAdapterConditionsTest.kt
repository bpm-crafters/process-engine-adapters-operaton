package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.NoOpServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.completion.NoOpUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.subscribe.SubscribingServiceTaskDelivery
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.operaton-remote.service-tasks.delivery-strategy = remote_scheduled",
    "dev.bpm-crafters.process-api.adapter.operaton-remote.user-tasks.delivery-strategy = remote_scheduled"
  ]
)
@ActiveProfiles("itest")
class OperatonRemoteAdapterScheduledStrategyConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThat(context.getBean(PullServiceTaskDelivery::class.java)).isNotNull()
    assertThat(context.getBean(ServiceTaskCompletionApi::class.java)).isNotNull()
    assertThat(context.getBean(PullUserTaskDelivery::class.java)).isNotNull()
  }

}


@SpringBootTest(
  properties = [
    "operaton.bpm.client.base-url = http://localhost:8080/engine-rest",
    "dev.bpm-crafters.process-api.adapter.operaton-remote.service-tasks.delivery-strategy = remote_subscribed",
    "dev.bpm-crafters.process-api.adapter.operaton-remote.user-tasks.delivery-strategy = remote_scheduled"
  ]
)
@ActiveProfiles("itest")
class OperatonRemoteAdapterSubscribedStrategyConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThat(context.getBean(SubscribingServiceTaskDelivery::class.java)).isNotNull()
    assertThat(context.getBean(ServiceTaskCompletionApi::class.java)).isNotNull()
    assertThat(context.getBean(PullUserTaskDelivery::class.java)).isNotNull()
  }

}

@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.operaton-remote.service-tasks.delivery-strategy = disabled",
    "dev.bpm-crafters.process-api.adapter.operaton-remote.user-tasks.delivery-strategy = disabled"
  ]
)
@ActiveProfiles("itest")
class OperatonRemoteAdapterDisabledConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(PullServiceTaskDelivery::class.java)
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(PullUserTaskDelivery::class.java)
    }
    assertThat(context.getBean(ServiceTaskCompletionApi::class.java)).isInstanceOf(NoOpServiceTaskCompletionApiImpl::class.java)
    assertThat(context.getBean(UserTaskCompletionApi::class.java)).isInstanceOf(NoOpUserTaskCompletionApiImpl::class.java)
  }

}

@SpringBootTest
@ActiveProfiles("withoutProps")
class OperatonRemoteAdapterWithoutPropsConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(PullServiceTaskDelivery::class.java)
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(ServiceTaskCompletionApi::class.java)
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(PullUserTaskDelivery::class.java)
    }
  }

}

