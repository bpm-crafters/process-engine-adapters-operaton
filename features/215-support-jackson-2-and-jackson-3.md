# Feature 215: Support Jackson 2 and Jackson 3

## Goal

Make serialization used by the Camunda 7 adapter work with both Jackson 2 and Jackson 3 so the adapter can support Spring Boot 3 and Spring Boot 4 without
keeping a hard Jackson 2 import in the core adapter modules.

## Current State

- `operaton-embedded-core` and `operaton-remote-core` directly depend on Jackson 2 types through `com.fasterxml.jackson.databind.ObjectMapper`.
- The direct coupling is currently in the decision evaluation path:
  - `EvaluateDecisionApiImpl`
  - `DelegatingDmnDecisionResult`
  - `DelegatingDmnDecisionEvaluationOutput`
  - `VariableMapDmnDecisionEvaluationOutput`
- `operaton-adapter-common` exists but currently does not provide shared serialization infrastructure.
- `operaton-embedded-spring-boot-starter` and `operaton-remote-spring-boot-starter` wire `ObjectMapper` directly into the adapter beans.

## Target Design

### 1. Introduce a serialization SPI in `operaton-adapter-common`

Add a minimal adapter-owned abstraction in `operaton-adapter-common` for the capability the adapter actually needs today: converting decision outputs into requested
target types.

Expected shape:

- One small interface, for example `AdapterSerialization` 
- A method equivalent to the current usage pattern, for example `<T : Any> convert(value: Any?, type: Class<T>): T?`

Design constraint:

- The SPI must not expose Jackson-specific types.
- The SPI should stay intentionally small and mirror current adapter needs, not the full Jackson API.

### 2. Provide two Jackson-specific implementations in `operaton-adapter-common`

Implement two adapters behind the common SPI:

- one implementation backed by Jackson 2
- one implementation backed by Jackson 3

Design constraint:

- Jackson-specific code stays out of `operaton-embedded-core` and `operaton-remote-core`.
- Only the implementation classes import the corresponding Jackson major version.
- `operaton-adapter-common` must not force either Jackson major onto consumers as a transitive dependency.

Dependency model:

- the Jackson 2 and Jackson 3 implementation classes in `operaton-adapter-common` compile against `provided` dependencies only
- `operaton-adapter-common` must not publish Jackson 2 or Jackson 3 as regular compile/runtime dependencies
- the consuming application or Spring Boot platform remains responsible for bringing the matching Jackson major onto the classpath
- the adapter should integrate with the surrounding ecosystem's JSON stack, not own or override it

### 3. Refactor core modules to depend only on the SPI

Replace all direct `ObjectMapper` constructor arguments in both core modules with the new common serialization abstraction.

Affected classes:

- embedded:
  - `engine-adapter/operaton-embedded-core/.../decision/EvaluateDecisionApiImpl.kt`
  - `engine-adapter/operaton-embedded-core/.../decision/DelegatingDmnDecisionResult.kt`
  - `engine-adapter/operaton-embedded-core/.../decision/DelegatingDmnDecisionEvaluationOutput.kt`
- remote:
  - `engine-adapter/operaton-remote-core/.../decision/EvaluateDecisionApiImpl.kt`
  - `engine-adapter/operaton-remote-core/.../decision/DelegatingDmnDecisionResult.kt`
  - `engine-adapter/operaton-remote-core/.../decision/VariableMapDmnDecisionEvaluationOutput.kt`

Result:

- both core modules depend on `operaton-adapter-common`
- both core modules no longer import Jackson 2 directly

### 4. Autoconfigure the correct implementation in both Spring Boot starters

At starter level, create the serialization bean based on available classes on the application classpath.

Required behavior:

- `operaton-embedded-spring-boot-starter` autoconfigures the Jackson 2-backed serializer when the Jackson 2 classes are available
- `operaton-embedded-spring-boot-starter` autoconfigures the Jackson 3-backed serializer when the Jackson 3 classes are available
- `operaton-remote-spring-boot-starter` does the same

Implementation notes:

- The main adapter auto-configuration classes should depend on the adapter SPI, not on `ObjectMapper`
- Jackson-major-specific bean creation should live in dedicated auto-configuration classes or nested configuration blocks guarded by `@ConditionalOnClass`
- Guard conditions must ensure only one serialization bean is created for a normal runtime
- If both Jackson majors are present, fail fast with a clear configuration message instead of silently picking one

### 5. Keep Spring Boot 3 and Spring Boot 4 behavior aligned

Boot compatibility should remain symmetric:

- Boot 3 path: existing Boot 3 starter usage should resolve to Jackson 2
- Boot 4 path: existing Boot 4 examples should resolve to Jackson 3

The existing Boot 4 embedded compatibility configuration must remain independent from the adapter-enabled flag. This feature should only change how
serialization is wired into adapter beans.

## Module and Build Changes

### Maven modules

- keep `engine-adapter/operaton-adapter-common` as the place for the SPI and the two serializer implementations
- add `operaton-adapter-common` as a dependency of both core modules

### Dependency scopes

- remove Jackson 2 as a required direct dependency from `operaton-embedded-core`
- keep Jackson dependencies out of the core modules
- compile the Jackson-major-specific implementations in `operaton-adapter-common` against `provided` dependencies
- do not expose Jackson 2 or Jackson 3 transitively from `operaton-adapter-common`
- keep starter modules dependent on the SPI and serializer bean wiring, not on exported Jackson libraries from adapter modules
- keep version alignment with the consuming Spring Boot BOM wherever possible

Expected dependency outcome:

- `operaton-embedded-core`: no direct Jackson dependency
- `operaton-remote-core`: no direct Jackson dependency
- `operaton-adapter-common`: Jackson 2 and Jackson 3 present only as `provided` compile-time inputs for the implementation classes
- consuming applications: obtain Jackson from Spring Boot or their own dependency graph

### BOM

Review `bom/pom.xml` and add `operaton-adapter-common` if the module becomes part of the supported public dependency surface.

## Implementation Steps

1. Add the serialization SPI to `operaton-adapter-common`.
2. Add the Jackson 2 and Jackson 3 implementations to `operaton-adapter-common`.
3. Refactor embedded decision evaluation classes to use the SPI.
4. Refactor remote decision evaluation classes to use the SPI.
5. Update both core POMs to depend on `operaton-adapter-common` and remove direct Jackson coupling from core.
6. Update both starter auto-configurations so decision API beans depend on the SPI.
7. Add Jackson-major-specific auto-configuration for embedded starter.
8. Add Jackson-major-specific auto-configuration for remote starter.
9. Verify Maven dependency scopes so no adapter artifact exports Jackson 2 or Jackson 3 transitively.
10. Register any new auto-configuration classes in `AutoConfiguration.imports`.
11. Validate Boot 3 and Boot 4 example applications.

## Validation

### Unit-level validation

- serializer SPI tests for:
  - empty decision output
  - single-entry output mapped to scalar or object
  - multi-entry output mapped to object
  - null handling
  - error message parity with current behavior

### Starter validation

- embedded starter test proving Jackson 2 serializer bean selection
- embedded starter test proving Jackson 3 serializer bean selection
- remote starter test proving Jackson 2 serializer bean selection
- remote starter test proving Jackson 3 serializer bean selection
- negative test for ambiguous classpath if both majors are present
- dependency-tree validation confirming adapter artifacts do not force Jackson onto consumers transitively

### Example validation

- `examples/java-operaton-embedded` still works on Boot 3
- `examples/java-operaton-remote` still works on Boot 3
- `examples/java-operaton-embedded-sb4` works on Boot 4
- `examples/java-operaton-remote` works on Boot 4

## Acceptance Criteria

- No production class in `operaton-embedded-core` or `operaton-remote-core` imports Jackson 2 directly.
- Serialization needed by decision evaluation is provided through `operaton-adapter-common`.
- Both Jackson 2 and Jackson 3 implementations exist and are selectable by classpath.
- Embedded and remote starters autoconfigure the matching serializer implementation.
- `operaton-adapter-common` does not export Jackson 2 or Jackson 3 as transitive compile/runtime dependencies.
- The effective Jackson version remains owned by the consuming ecosystem, typically the Spring Boot BOM.
- Boot 3 examples resolve the Jackson 2 path.
- Boot 4 examples resolve the Jackson 3 path.
- Existing decision output mapping semantics remain unchanged for consumers.

## Risks and Notes

- The exact Jackson 3 mapper type and Maven coordinates must be used consistently in the classpath conditions and compile dependencies.
- `provided` scope and published module metadata must be checked carefully so Jackson is not accidentally reintroduced as a transitive dependency through another adapter module.
- Avoid widening the SPI beyond the current adapter need; otherwise the migration will create unnecessary surface area.
- If additional serialization entry points exist beyond decision evaluation, fold them onto the same SPI in the same feature instead of introducing a second
  abstraction.
