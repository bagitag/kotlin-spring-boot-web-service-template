# Testing

Overview of the testing frameworks and tools used in this project.

---

<details>
  <summary>Table of Contents</summary>

- [Technology Stack](#technology-stack)
- [Unit Tests](#unit-tests)
- [Integration Tests](#integration-tests)
- [Test Coverage](#test-coverage)

</details>

## Technology stack

- [JUnit 5](https://junit.org/junit5/)
- [MockK](https://mockk.io/)
- [Testcontainers](https://github.com/testcontainers/)
- [WireMock](https://wiremock.org/)
- [JaCoCo](https://www.eclemma.org/jacoco/)

## Unit tests

Unit tests are executed automatically during the build's `test` phase. Use the following command to run them:

```mvn test```

Coverage reports for unit tests are generated per module in the `target/site/jacoco-unit-test-report` directory using the `jacoco-maven-plugin`. The plugin configuration is in the root [pom.xml](../pom.xml).

### Special unit test class

The [TemplateApplicationTests.kt](../template-project-web/src/test/kotlin/com/example/templateproject/TemplateApplicationTests.kt) file is not a typical unit test. It spins up the entire Spring application context to verify that the application is able to start without any issues.

This class also ensures the [openapi.yaml](../openapi.yaml) file is kept in sync with the code to catch any mismatches between the OpenAPI specification and the actual API implementation.

The [openapi.yaml](../openapi.yaml) file can be updated by running the following command:

```mvn clean verify -Popenapi```

## Integration tests    

Test files ending with `IT` are executed during the `verify` phase. These tests require a running database. By default, Testcontainers is used to start a PostgreSQL database in Docker. See the configuration here: [TestcontainersConfig.kt](../template-project-web/src/test/kotlin/com/example/templateproject/web/TestcontainersConfig.kt).

If Docker is not available, enable the development Maven profile to use an in-memory database by adding `-Pdev` the following to the Maven command:

```mvn verify```

Integration test coverage reports are generated per module in the `target/site/jacoco-integration-test-report` directory using the `jacoco-maven-plugin`. The plugin configuration is in the root [pom.xml](../pom.xml).

### Testing external service calls

WireMock is used to mock HTTP services. Running [JsonPlaceholderIT.kt](../template-project-web/src/test/kotlin/com/example/templateproject/web/controller/JsonPlaceholderIT.kt) starts a WireMock server.

## Test coverage

Coverage rules and limits are defined in the root [pom.xml](../pom.xml) under the `jacoco-maven-plugin` configuration. The build is configured to fail if the coverage does not meet the specified thresholds.

### The jacoco-report module

This module is used to generate an aggregated JaCoCo coverage report that combines unit and integration coverage from all modules. The aggregated report can be found here after a successful build: [template-project-jacoco-report/target/site/jacoco-aggregate/index.html](../template-project-jacoco-report/target/site/jacoco-aggregate/index.html)
