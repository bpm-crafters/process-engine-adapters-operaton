package dev.bpmcrafters.example.javaoperaton;

import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.springboot.OperatonEmbeddedAdapterAutoConfiguration;
import dev.bpmcrafters.processengineapi.adapter.operaton.embedded.task.delivery.pull.EmbeddedPullServiceTaskDeliveryMetrics;
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterAutoConfiguration;
import dev.bpmcrafters.processengineapi.adapter.operaton.remote.task.delivery.pull.PullServiceTaskDeliveryMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.LazyInitializationBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTaskDeliveryMetricsTest {

  @Test
  void remoteMetricsAreRecordedInTheAutoConfiguredRegistry() {
    try (var context = metricsContext()) {
      context.getBean(PullServiceTaskDeliveryMetrics.class).incrementFetchedAndLockedTasksCounter("example", 2);

      assertThat(context.getBean(MeterRegistry.class)
        .get("bpmcrafters.operaton.remote.scheduled.external.tasks.fetched.and.locked")
        .tag("topic", "example").counter().count()).isEqualTo(2);
    }
  }

  @Test
  void embeddedMetricsAreRecordedInTheAutoConfiguredRegistry() {
    try (var context = metricsContext()) {
      context.getBean(EmbeddedPullServiceTaskDeliveryMetrics.class).incrementFetchedAndLockedTasksCounter("example", 2);

      assertThat(context.getBean(MeterRegistry.class)
        .get("bpmcrafters.operaton.embedded.scheduled.external.tasks.fetched.and.locked")
        .tag("topic", "example").counter().count()).isEqualTo(2);
    }
  }

  private AnnotationConfigApplicationContext metricsContext() {
    var context = new AnnotationConfigApplicationContext();
    context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("adapter-metrics", Map.of(
      "dev.bpm-crafters.process-api.adapter.operaton-remote.enabled", true,
      "dev.bpm-crafters.process-api.adapter.operaton-embedded.enabled", true
    )));
    // Evaluate the real auto-configuration conditions without starting an Operaton engine.
    context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
    context.register(MetricsConfiguration.class);
    context.refresh();
    return context;
  }

  @Configuration(proxyBeanMethods = false)
  @ImportAutoConfiguration({
    OperatonRemoteAdapterAutoConfiguration.class,
    OperatonEmbeddedAdapterAutoConfiguration.class,
    MetricsAutoConfiguration.class,
    SimpleMetricsExportAutoConfiguration.class,
    CompositeMeterRegistryAutoConfiguration.class
  })
  static class MetricsConfiguration {
  }
}
