# Spring Boot Web Service Template

[![Build Status](https://github.com/bagitag/kotlin-spring-boot-web-service-template/actions/workflows/build.yml/badge.svg)](https://github.com/bagitag/kotlin-spring-boot-web-service-template/actions/workflows/build.yml)

[JaCoCo coverage](https://bagitag.github.io/kotlin-spring-boot-web-service-template/)

This is a template project for a simple Spring Boot REST service written in Kotlin. It can be used to quickly create a new web service.

The project contains a lot of useful features and configurations that are commonly used in web services.
For more details, see the [Features](#features) section.

---

# Table of Contents

- [Technical Overview](#technical-overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [How to build the project](#how-to-build-the-project)
- [How to run the application](#how-to-run-the-application)
- [Configuration](#configuration)
- [Further documentation](#further-documentation)

# Technical Overview

## Architecture

```mermaid

flowchart LR
    Client[Client] <--> Web(Controller Layer)
    Web <--> Service(Service Layer)
    Service <--> Repository(Repository Layer)
    Repository <--> DB[(Database)]
    Service <--> ExternalService[External Service]
```

## Project Structure

The project is structured into the following modules:

- **api**: Defines the request and response structures for the service.
- **client**: Contains other service integrations. See details: [here](template-project-client/CLIENT.md)
- **core**: Contains the core functionality of the application. It acts as a bridge between the web, client and persistence layers.
- **persistence**: Contains the data access layer of the application. See the details: [here](template-project-persistence/PERSISTENCE.md)
- **web**: Contains the web layer of the application, including controllers and exception handling.
- **jacoco-report**: Used for generating the aggregated JaCoCo coverage report.

## Technology Stack

Kotlin, Spring Boot, Maven

### Database

Liquibase, PostgreSQL

### Testing

JUnit 5, MockK, Testcontainers

### Monitoring
Micrometer, OpenTelemetry, Prometheus, Grafana

### Code Quality
Detekt, Ktlint, JaCoCo

### DevOps & CI/CD
Docker, GitHub Actions

# Getting Started

## How to build the project

### Prerequisites

The application needs to connect to a database to be able to run the integration tests. By default, Testcontainers is used to start a database in a Docker container. If you do not have Docker installed please enable the development Maven profile, to use an in-memory database, by adding the following to the Maven command: ```-Pdev```

Further details about running tests can be found [here](./.document/TESTING.md).

### Build the project

Maven is used as the build tool. To build the project, run the following command:

```mvn clean install```

Or if you do not have Maven installed, you can use the Maven Wrapper that is included in the project:

```bash
 ./mvnw clean install
```

### Other build commands (from fastest to slowest):

- Build the project without compiling the tests:

```mvn clean install -Dmaven.test.skip=true```

- Build the project without running the tests:

```mvn clean install -DskipTests```

- Build the project without the integration tests, running only the unit tests:

```mvn clean install -DskipITs```


## How to run the application

### Prerequisites

To be able to run the application, you must comply with the followings:
- Have Java installed on your machine. Supported version is 21.
- Have a running database. The application is configured to connect to a PostgreSQL database.
- Properly configure the application. See the [Configuration](#configuration) section for more information.

### Run the application

To run the application, execute the following command:

```mvn spring-boot:run```

Or if you do not have Maven installed, you can use the Maven Wrapper that is included in the project:

```bash 
 ./mvnw spring-boot:run
```

Another way to run the application, that only requires Docker to be installed, is to use the [Docker Compose file](docker-compose.yml) that is included in the project. To run the application using Docker Compose, execute the following command:

```bash
 docker-compose up
```

## Configuration

Starting the application with the default Spring Boot profile will not work, as the application requires configuration to be set.  
This way the app cannot be started without proper configuration in production.

The minimum required configuration can be found in the [docker-compose.yml](./docker-compose.yml) file under the `environment` section of the `server` service.

The full list of configuration properties can be found in the `*-dev.properties` file in each module's `src/main/resources` directory.

## Further documentation

Detailed documentation can be found for the following topics:

- [Development](./.document/DEVELOPMENT.md)
- [Testing](./.document/TESTING.md)
