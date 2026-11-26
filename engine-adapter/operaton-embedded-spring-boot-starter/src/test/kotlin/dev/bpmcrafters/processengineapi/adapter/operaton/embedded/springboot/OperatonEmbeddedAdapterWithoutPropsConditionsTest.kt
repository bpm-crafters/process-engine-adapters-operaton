package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("withoutProps")
class OperatonEmbeddedAdapterWithoutPropsConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean<EmbeddedPullServiceTaskDelivery>()
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean<EmbeddedPullUserTaskDelivery>()
    }
  }
}
