# Java Example to demonstrate usage of process API using the remote adapter and Jackson 3

This example is a test that we can invoke API defined in Kotlin from Java. It utilizes the API directly and talks to
an Operaton engine running in Docker via its Camunda-7-compatible REST API.

The remote adapter does not depend on Spin, so this example is the straightforward Jackson 3 path. The starter
auto-configures the adapter serialization from the Jackson 3 mapper available in the Spring Boot 4 ecosystem.

Two things are Jackson-2-specific: the community REST client (`c7-rest-client`) maps engine values with a Jackson 2
`ObjectMapper`, which Spring Boot 4 no longer auto-configures — the application therefore declares one explicitly.
And because that mapper deserializes the user task payload records via their constructors, the example adds
`jackson-module-parameter-names` to the runtime classpath.

## Features in the example

There are some features in the Operaton remote adapter already. In addition, there are some features in the example:

- AbstractSynchronousTaskHandler to complete external tasks in a synchronous way
- In-Memory user task pool for retrieving infos about open user tasks

## Process

![Service Task Process](src/main/resources/simple-process.png)


## How to run

- Start docker-compose
- Build with Maven
- Start `JavaOperatonRemoteExampleApplication` from your IDE, or from the command line:
  `java -jar target/process-engine-api-example-java-operaton-remote-*.jar`
  (alternatively `../../mvnw spring-boot:run` in this directory)
- Open http://localhost:8081/swagger-ui/index.html
- Start process
- Wait, wait, wait, check the logs, wait...
- Copy the resulting retrieved user task id
- Complete the user task with id
- Wait, wait, wait, check the logs, wait...
- Correlate message by providing the process instance id
- Hint: don't hurry, the error of correlation is not implemented yet (if you try it before both tasks are executed)

## How to run using IntelliJ test script
- Build with Maven
- Start `JavaOperatonRemoteExampleApplication`
- Run `simple-process-demo.http` script
- Analyze the results
- Run `simple-process-demo-failed-user.http` script
- Analyze the results
