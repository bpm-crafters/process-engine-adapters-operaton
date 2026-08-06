package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.operaton-embedded.service-tasks.delivery-strategy = embedded_scheduled",
    "dev.bpm-crafters.process-api.adapter.operaton-embedded.user-tasks.delivery-strategy = embedded_scheduled"
  ]
)
@ActiveProfiles("itest")
class OperatonEmbeddedAdapterScheduledStrategyConditionsITest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThat(context.getBean<EmbeddedPullServiceTaskDelivery>()).isNotNull()
    assertThat(context.getBean<ServiceTaskCompletionApi>()).isNotNull()
    assertThat(context.getBean<EmbeddedPullUserTaskDelivery>()).isNotNull()
  }

}

@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.operaton-embedded.service-tasks.delivery-strategy = disabled",
    "dev.bpm-crafters.process-api.adapter.operaton-embedded.user-tasks.delivery-strategy = disabled"
  ]
)
@ActiveProfiles("itest")
class OperatonEmbeddedAdapterDisabledConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    org.junit.jupiter.api.assertThrows<NoSuchBeanDefinitionException> {
      context.getBean<EmbeddedPullServiceTaskDelivery>()
    }
    org.junit.jupiter.api.assertThrows<NoSuchBeanDefinitionException> {
      context.getBean<EmbeddedPullUserTaskDelivery>()
    }
  }

}

@SpringBootTest
@ActiveProfiles("withoutProps")
class OperatonEmbeddedAdapterWithoutPropsConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    org.junit.jupiter.api.assertThrows<NoSuchBeanDefinitionException> {
      context.getBean<EmbeddedPullServiceTaskDelivery>()
    }
    org.junit.jupiter.api.assertThrows<NoSuchBeanDefinitionException> {
      context.getBean<EmbeddedPullUserTaskDelivery>()
    }
  }

}

