package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.operaton-remote.service-tasks.delivery-strategy = custom",
    "dev.bpm-crafters.process-api.adapter.operaton-remote.user-tasks.delivery-strategy = disabled"
  ]
)
@ActiveProfiles("itest")
class OperatonRemoteCustomServiceTaskConditionsITest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun `does not provide service completion API for custom delivery`() {
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(PullServiceTaskDelivery::class.java)
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(ServiceTaskCompletionApi::class.java)
    }
  }
}
