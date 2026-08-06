# Examples

All examples run on the root Spring Boot version (4.0.x) against Operaton `2.1.3`.

## Operaton embedded

Module: `java-operaton-embedded`

Port: 8082

Serialization profile: embedded Operaton with Spin JSON serialization and Jackson 2. Also ships the Operaton webapp
(login `admin` / `admin`).

## Operaton embedded without Spin

Module: `java-operaton-embedded-jackson3`

Port: 8083

Serialization profile: embedded Operaton without Spin and with Jackson 3.

## Operaton remote

Module: `java-operaton-remote`

Port: 8081

Serialization profile: remote adapter with Jackson 3. The Operaton engine runs in Docker (see the module's
`docker-compose.yaml`).

## Jackson configuration summary

- Spin JSON serialization works with Jackson 2 only.
- Embedded Operaton with Spin should use the Jackson-2 example.
- Embedded Operaton with Jackson 3 should not configure Spin JSON serialization.
- Remote adapter setups can use Jackson 3 because they are not bound to embedded Spin.
