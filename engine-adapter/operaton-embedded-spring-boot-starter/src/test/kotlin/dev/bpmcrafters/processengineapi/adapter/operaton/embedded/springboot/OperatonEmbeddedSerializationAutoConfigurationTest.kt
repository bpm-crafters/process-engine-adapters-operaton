package dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.operaton.common.serialization.AdapterDataConverter
import dev.bpmcrafters.processengineapi.adapter.operaton.common.serialization.Jackson3AdapterDataConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("itest")
class OperatonEmbeddedSerializationAutoConfigurationTest {

  @Autowired
  lateinit var dataConverter: AdapterDataConverter

  @Test
  fun `should prefer jackson 3 data converter when spring boot 4 provides both mappers`() {
    assertThat(dataConverter).isInstanceOf(Jackson3AdapterDataConverter::class.java)
  }

}
