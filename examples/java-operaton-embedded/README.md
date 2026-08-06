# Java Example to demonstrate usage of process API with Spin and Jackson 2

This example is a test that we can invoke API defined in Kotlin from Java. It utilizes the API directly and runs an
embedded Operaton engine in a Spring Boot 4 application.

The example uses the public Operaton artifacts in version `2.1.3`. The example adds `spring-boot-starter-jdbc`,
because the embedded engine needs a `DataSource` and transaction manager.

This variant keeps Operaton Spin with the JSON-Jackson data format on the classpath. Because Spin still depends on the
Jackson 2 ecosystem, the application provides an explicit `AdapterDataConverter` bean backed by a Jackson 2
`ObjectMapper`. That keeps the adapter serialization path aligned with Spin.

The example also ships the Operaton webapp (Cockpit/Tasklist), reachable at http://localhost:8082/operaton/ with the
credentials `admin` / `admin`.

## Features in the example

There are some features in the Operaton adapter already. In addition, there are some features in the example:

- AbstractSynchronousTaskHandler to complete external tasks in a synchronous way
- In-Memory user task pool for retrieving infos about open user tasks

## Process

![Service Task Process](src/main/resources/simple-process.png)


## How to run

- Build with Maven
- Start `JavaOperatonExampleApplication`
- Open http://localhost:8082/swagger-ui/index.html
- Start process
- Wait, wait, wait, check the logs, wait...
- Copy the resulting retrieved user task id
- Complete the user task with id
- Wait, wait, wait, check the logs, wait...
- Correlate message by providing the generated correlation key
- Hint: don't hurry, the error of correlation is not implemented yet (if you try it before both tasks are executed)

## How to run using IntelliJ test script
- Build with Maven
- Start `JavaOperatonExampleApplication`
- Run `simple-process-demo.http` script
- Analyze the results
- Run `simple-process-demo-failed-user.http` script
- Analyze the results
