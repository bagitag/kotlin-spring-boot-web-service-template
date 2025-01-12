# Spring Boot Web Service Template

[![Build Status](https://github.com/bagitag/kotlin-spring-boot-web-service-template/actions/workflows/build.yml/badge.svg)](https://github.com/bagitag/kotlin-spring-boot-web-service-template/actions/workflows/build.yml)

[JaCoCo coverage](https://bagitag.github.io/kotlin-spring-boot-web-service-template/)

This is a template project for a simple SpringBoot REST service written in Kotlin. It can be used to quickly create a new web service.

The project contains a lot of useful features and configurations that are commonly used in web services.

---

# Table of Contents

- [Getting Started](#getting-started)
- [Architecture](#architecture)
- [How to build the project](#how-to-build-the-project)
- [How to run the application](#how-to-run-the-application)
- [Project Structure](#project-structure)
- 
- [Features](#features)
- [Configuration](#configuration)
- [Development](#development)
- [Testing](#testing)
- [Monitoring](#monitoring)

# Getting Started

## Architecture

## How to build the project

### Prerequisites

The application needs to connect to a database to be able to run the integration tests. By default, [Testcontainers](https://github.com/testcontainers/) is used to start a database in a Docker container. If you do not have Docker installed please enable the development Maven profile, to use an in-memory database, by adding the following profile to the Maven command:

```-Pdev```

### Build the project

Maven is used as the build tool. To build the project, run the following command:

```mvn clean install```

Or if you do not have Maven installed, you can use the Maven Wrapper that is included in the project:

```./mvnw clean install```

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

```./mvnw spring-boot:run```

Another way to run the application, that only requires Docker to be installed, is to use the [Docker Compose file](docker-compose.yml) that is included in the project. To run the application using Docker Compose, execute the following command:

```docker-compose up```

For development purposes, please see the [Development](#development) section.

## Project Structure

The project is structured into the following Maven modules:

- **api**: Contains the REST API of the application.
- **client**: Contains the client of the application.
- **core**: Contains the core functionality of the application.
- **persistence**: Contains the data access layer of the application.
- **web**: Contains the web layer of the application.

## Configuration

## Development

