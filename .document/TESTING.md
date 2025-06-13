# Testing

<details>
  <summary>Table of Contents</summary>

- [Technology Stack](#technology-stack)
- [Unit Tests](#unit-tests)
  - [Special Unit Test Class](#special-unit-test-class)
- [Integration Tests](#integration-tests)
  - [Testing External Service Calls](#testing-external-service-calls)
- [Test Coverage](#test-coverage)
  - [The jacoco-report Module](#the-jacoco-report-module)

</details>

## Technology stack

- [JUnit 5](https://junit.org/junit5/)
- [MockK](https://mockk.io/)
- [Testcontainers](https://github.com/testcontainers/)
- [WireMock](https://wiremock.org/)
- [JaCoCo](https://www.eclemma.org/jacoco/)

## Unit tests

During the build process, unit tests are executed automatically during the `test` phase. To run the unit tests, use the following command:

```mvn test```

Coverage reports are generated in the `target/site/jacoco-unit-test-report` directory for each module with the help of the `jacoco-maven-plugin`. Configuration for the plugin is located in the root [pom.xml](../pom.xml) file.

### Special unit test class

The [TemplateApplicationTests.kt](../template-project-web/src/test/kotlin/com/example/templateproject/TemplateApplicationTests.kt) file is not a unit test in the traditional sense. It spins up the entire Spring application context to verify that the application is able to start without any issues.

This class also ensures that the [openapi.yaml](../openapi.yaml) file is in sync with the code. Its goal is to catch any discrepancies between the OpenAPI specification and the actual API implementation.

The [openapi.yaml](../openapi.yaml) file can be updated by running the following command:

```mvn clean verify -Popenapi```

## Integration tests

Test files ending with `IT` are executed during the `verify` phase. Running these tests requires a running database. By default, Testcontainers is used to start a PostgreSQL database in a Docker container. See the configuration here: [TestcontainersConfig.kt](../template-project-web/src/test/kotlin/com/example/templateproject/web/TestcontainersConfig.kt).

If you do not have Docker installed, you can enable the development Maven profile to use an in-memory database by adding `-Pdev` the following to the Maven command:

```mvn verify```

Coverage reports are generated in the `target/site/jacoco-integration-test-report` directory for each module with the help of the `jacoco-maven-plugin`. Configuration for the plugin is located in the root [pom.xml](../pom.xml) file.

### Testing external service calls

WireMock is used to mock HTTP services. Running [JsonPlaceholderIT.kt](../template-project-web/src/test/kotlin/com/example/templateproject/web/controller/JsonPlaceholderIT.kt) will start a WireMock server.

## Test coverage

Rules and limits for test coverage are defined in the root [pom.xml](../pom.xml) file, under the `jacoco-maven-plugin` configuration. The build is configured to fail if the coverage does not meet the specified thresholds.

### The jacoco-report module

This module is used to generate an aggregated JaCoCo coverage report for both unit and integration tests. It combines the coverage data from all modules and generates a single report. The aggregated report can be found here: [index.html](../template-project-jacoco-report/target/site/jacoco-aggregate/index.html)
