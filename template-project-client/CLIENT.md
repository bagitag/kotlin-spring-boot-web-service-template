# Client module

This module handles integration with third-party services and external APIs. The project uses
[JSONPlaceholder](https://jsonplaceholder.typicode.com/) as an example.

---

<details>
  <summary>Table of Contents</summary>

- [Technology Stack](#technology-stack)
- [Features](#features)
  - [Circuit Breaker](#circuit-breaker)
  - [Retry](#retry)
  - [Caching](#caching)
  - [Thread pool](#thread-pool)
  - [Logging](#logging)
- [Configuration](#configuration)

</details>

## Technology Stack

- RestClient
- [HTTP interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
- Caching with [Caffeine](https://github.com/ben-manes/caffeine)
- Spring Retry
- Circuit Breaker pattern

## Features

### Circuit Breaker

The module uses the Spring Retry to implement the Circuit Breaker pattern. See the implementation in [JsonPlaceholderCircuitBreaker](src/main/kotlin/com/example/templateproject/client/jsonplaceholder/configuration/JsonPlaceholderCircuitBreaker.kt).

### Retry

The module uses the Spring Retry to retry failed requests. The retry configuration is defined in [RetryableHttpRequestDecorator](src/main/kotlin/com/example/templateproject/client/RetryableHttpRequestDecorator.kt).

### Caching

Responses from the external service are cached using Caffeine. The caching configuration is in [JsonPlaceholderCacheConfiguration](src/main/kotlin/com/example/templateproject/client/jsonplaceholder/configuration/JsonPlaceholderCacheConfiguration.kt).

### Thread pool

A thread pool is configured to execute the requests to the external service. The configuration can be found in the [JsonPlaceholderConfiguration](src/main/kotlin/com/example/templateproject/client/jsonplaceholder/configuration/JsonPlaceholderConfiguration.kt) class.

The pool is used with the help of Spring Boot's asynchronous support via the `@Async` annotation. See details in [JsonPlaceholderService](src/main/kotlin/com/example/templateproject/client/jsonplaceholder/JsonPlaceholderService.kt).

### Logging

A `TaskDecorator` is configured on the thread pool to ensure MDC (Mapped Diagnostic Context) is propagated to worker threads. See [MdcDecorator](src/main/kotlin/com/example/templateproject/client/MdcDecorator.kt) for details.

The service communication related logs are prefixed with `[service_name]` for easier log filtering.

## Configuration

Configuration can be found in the **client-\[spring-profile].properties** files.

### Dev profile

- Connects to the [JSONPlaceholder](https://jsonplaceholder.typicode.com/) service.
- The retry and caching functionalities are disabled.

### Docker profile

- Uses a [json-server](https://www.npmjs.com/package/json-server) as a mock service that is started in a Docker container based on the [docker-compose-dev.yml](../docker-compose-dev.yml) file.
- The retry and caching functionalities are enabled.
