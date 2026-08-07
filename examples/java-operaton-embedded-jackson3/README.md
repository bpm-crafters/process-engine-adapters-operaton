# Java Example to demonstrate usage of process API without Spin and with Jackson 3

This example is a test that we can invoke API defined in Kotlin from Java. It utilizes the API directly and runs an
embedded Operaton engine in a Spring Boot 4 application.

The example adds `spring-boot-starter-jdbc`, because the embedded engine needs a `DataSource` and transaction
manager.

This variant deliberately does not add Operaton Spin. That leaves the adapter free to auto-configure against the
Jackson 3 mapper provided by Spring Boot 4. The sample process still returns a complex object variable, so this example
relies on the engine's default Java serialization for that variable instead of forcing JSON serialization.

## Features in the example

There are some features in the Operaton adapter already. In addition, there are some features in the example:

- AbstractSynchronousTaskHandler to complete external tasks in a synchronous way
- In-Memory user task pool for retrieving infos about open user tasks

## How to run

- Build with Maven
- Start `JavaOperatonExampleApplication` from your IDE, or from the command line:
  `java -jar target/process-engine-api-example-java-operaton-embedded-jackson3-*.jar`
  (alternatively `../../mvnw spring-boot:run` in this directory)
- Open http://localhost:8083/swagger-ui/index.html
- Start process
- Wait, wait, wait, check the logs, wait...
- Copy the resulting retrieved user task id
- Complete the user task with id
- Wait, wait, wait, check the logs, wait...
- Correlate message by providing the generated correlation key
- Hint: don't hurry, the error of correlation is not implemented yet (if you try it before both tasks are executed)
