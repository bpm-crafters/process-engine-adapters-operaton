package dev.bpmcrafters.example.javaoperaton;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.bpmcrafters.processengineapi.adapter.operaton.common.serialization.AdapterDataConverter;
import dev.bpmcrafters.processengineapi.adapter.operaton.common.serialization.Jackson2AdapterDataConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Embedded example with Operaton Spin.
 */
@SpringBootApplication
public class JavaOperatonExampleApplication {

  @Bean
  public AdapterDataConverter adapterDataConverter() {
    return new Jackson2AdapterDataConverter(new ObjectMapper().findAndRegisterModules());
  }

  public static void main(String[] args) {
    SpringApplication.run(JavaOperatonExampleApplication.class, args);
  }
}
