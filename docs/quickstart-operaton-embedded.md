---
title: Operaton as embedded engine
---

If you start with Operaton, operated in an embedded engine mode, by for example using the Operaton Spring Boot Starter,
the following configuration is applicable for you.

First of all, add the corresponding adapter and an embedded Operaton starter to your project's classpath:

```xml 
<dependencies>
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-adapters</groupId>
    <artifactId>process-engine-adapter-operaton-embedded-spring-boot-starter</artifactId>
    <version>${process-engine-adapter-operaton.version}</version>
  </dependency>
  <dependency>
    <groupId>org.operaton.bpm.springboot</groupId>
    <artifactId>operaton-bpm-spring-boot-starter</artifactId>
    <version>2.1.3</version>
  </dependency>
</dependencies>
```

## Spring Boot 4

The adapter starter and the Operaton 2.x starters require Spring Boot 4. Runnable examples are available in
`examples/java-operaton-embedded` (with Spin and Jackson 2) and `examples/java-operaton-embedded-jackson3`
(without Spin, with Jackson 3).

The consuming application needs the normal embedded-engine runtime dependencies, including a JDBC starter or
equivalent `DataSource`/transaction-manager setup — since the Spring Boot 4 module split, add
`spring-boot-starter-jdbc` (or `-data-jpa`) explicitly.

If you add the Operaton webapp starter (`operaton-bpm-spring-boot-starter-webapp`), log in with the admin user you
configure under `operaton.bpm.admin-user` (the examples use `admin` / `admin`).

If process variables contain custom objects and `operaton.bpm.default-serialization-format` is set to
`application/json`, add Operaton Spin with the JSON-Jackson data format, for example:

```xml
  <dependency>
    <groupId>org.operaton.bpm</groupId>
    <artifactId>operaton-engine-plugin-spin</artifactId>
  </dependency>
  <dependency>
    <groupId>org.operaton.spin</groupId>
    <artifactId>operaton-spin-dataformat-json-jackson</artifactId>
  </dependency>
```

### Jackson and Spin combinations

The adapter itself supports Jackson 2 and Jackson 3, but the embedded-engine runtime has an extra constraint:

- Operaton Spin JSON support works with the Jackson 2 ecosystem only.
- If you use Spin for JSON variable serialization in an embedded Operaton setup, keep the adapter on Jackson 2 as well.
- If you want Jackson 3 with the embedded adapter, do not configure Spin JSON serialization.
- In the embedded Jackson-3 path, avoid `operaton.bpm.default-serialization-format=application/json` unless you replace Spin with a different compatible serialization approach.

In practice this means:

- Embedded + Spin + JSON object variables: Jackson 2
- Embedded + no Spin: Jackson 3 can work
- Remote adapter: Jackson 3 can work because the adapter does not depend on embedded Spin

and finally, add the following configuration to your configuration properties. Here is a version for `application.yaml`:

```yaml 
dev:
  bpm-crafters:
    process-api:
      adapter:
        operaton-embedded:
          enabled: true
          service-tasks:
            delivery-strategy: embedded_scheduled
            worker-id: embedded-worker
            max-task-count: 100
            lock-time-in-seconds: 10
            retry-timeout-in-seconds: 30
            retries: 3
            execute-initial-pull-on-startup: true
            schedule-delivery-fixed-rate-in-seconds: 10
          user-tasks:
            delivery-strategy: embedded_scheduled
            execute-initial-pull-on-startup: true
            schedule-delivery-fixed-rate-in-seconds: 10

```
