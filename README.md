# Process Engine Adapter Operaton

[![incubating](https://img.shields.io/badge/lifecycle-INCUBATING-orange.svg)](https://github.com/holisticon#open-source-lifecycle)
[![Development branches](https://github.com/bpm-crafters/process-engine-adapters-operaton/actions/workflows/development.yml/badge.svg)](https://github.com/bpm-crafters/process-engine-adapters-operaton/actions/workflows/development.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/dev.bpm-crafters.process-engine-adapters/process-engine-adapter-operaton-bom)](https://maven-badges.herokuapp.com/maven-central/dev.bpm-crafters.process-engine-adapters/process-engine-adapter-operaton-bom)
[![Compatible with Operaton](https://img.shields.io/badge/Compatible%20with-Operaton%202.1.3-1a7f5a.svg)](https://operaton.org)

## Purpose of the library

This library provides an adapter implementation of [Process Engine API](https://github.com/bpm-crafters/process-engine-api) for the [Operaton](https://operaton.org) process engine.

It is derived from the [Camunda 7 adapter](https://github.com/bpm-crafters/process-engine-adapters-camunda-7): Operaton
is an API-compatible open-source fork of Camunda 7, so both the embedded and the remote adapter carry over. The remote
adapter keeps using the community REST client for Camunda 7, which works against Operaton's Camunda-7-compatible REST
API.

## 📚 Documentation

The documentation can be found [here](https://bpm-crafters.github.io/process-engine-api-docs/stable/) or in its
respective [repository](https://github.com/bpm-crafters/process-engine-api-docs).

## Compatibility

| Adapter Version | Operaton Version | API Version | Spring Boot |
|-----------------|------------------|-------------|-------------|
| [2026.08.1](https://github.com/bpm-crafters/process-engine-adapters-operaton/releases/tag/2026.08.1) | 2.1.3 | 1.7 | 4.0 |

## Usage

If you want to start usage, please add the BOM to your Maven project and add the corresponding adapter implementation:

```xml
<dependency>
  <groupId>dev.bpm-crafters.process-engine-adapters</groupId>
  <artifactId>process-engine-adapter-operaton-bom</artifactId>
  <version>${process-engine-adapter-operaton.version}</version>
  <scope>import</scope>
  <type>pom</type>
</dependency>
```

## Anatomy

The library contains of the following Maven modules:

- `process-engine-adapter-operaton-adapter-common`: shared functionality used by both adapter implementations
- `process-engine-adapter-operaton-embedded-core`: Operaton Embedded Adapter implementation
- `process-engine-adapter-operaton-embedded-spring-boot-starter`: Operaton Embedded Adapter Spring Boot Starter
- `process-engine-adapter-operaton-remote-core`: Operaton Remote Adapter implementation
- `process-engine-adapter-operaton-remote-spring-boot-starter`: Operaton Remote Adapter Spring Boot Starter
- `process-engine-adapter-operaton-testing`: test fixtures and utilities for testing the adapters
- `process-engine-adapter-operaton-bom`: Maven BOM containing dependency definitions.
