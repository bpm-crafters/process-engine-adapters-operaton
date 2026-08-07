package dev.bpmcrafters.example.javaoperaton;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Remote example talking to Operaton via the Camunda-7-compatible REST API.
 */
@SpringBootApplication
public class JavaOperatonRemoteExampleApplication {

  /**
   * The c7-rest-client value mapper requires a Jackson 2 {@link ObjectMapper} bean, which Spring Boot 4 no longer
   * auto-configures (it only provides a Jackson 3 mapper). The adapter itself keeps using Jackson 3 for payload
   * serialization; this mapper is only used by the REST client.
   */
  @Bean
  public ObjectMapper jackson2ObjectMapper() {
    return new ObjectMapper().findAndRegisterModules();
  }

  public static void main(String[] args) {
    SpringApplication.run(JavaOperatonRemoteExampleApplication.class, args);
  }

}
