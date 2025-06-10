# Client module

This module is responsible for integrating with third-party services and external APIs.
[JSONPlaceholder](https://jsonplaceholder.typicode.com/) is used as an example.

<details open>
  <summary>Table of Contents</summary>

- [Technology Stack](#technology-stack)
- [Configuration](#configuration)
- [Features](#features)

</details>

## Technology Stack

- RestClient
- [HTTP interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
- Caching with [Caffeine](https://github.com/ben-manes/caffeine)
- Spring Retry
- Circuit Breaker pattern

## Configuration

The configuration can be found in the **client-\[spring-profile].properties** files.

### Dev profile

- Connects to the [JSONPlaceholder](https://jsonplaceholder.typicode.com/) service.
- The retry and caching functionalities are disabled.


### Docker profile

- Uses a [json-server](https://www.npmjs.com/package/json-server) as a mock service that is started in a Docker container based on the [docker-compose-dev.yml](../docker-compose-dev.yml) file.
- The retry and caching functionalities are enabled.

## Features

### Circuit Breaker

The client module uses the Spring Retry implement of the Circuit Breaker pattern. See details in [JsonPlaceholderCircuitBreaker](src/main/kotlin/com/example/templateproject/client/jsonplaceholder/JsonPlaceholderCircuitBreaker.kt).

### Retry

The client module uses the Spring Retry to retry failed requests. The retry configuration can be found in the [RetryableHttpRequestDecorator](src/main/kotlin/com/example/templateproject/client/RetryableHttpRequestDecorator.kt) class.

### Caching

The client module caches the responses from the external service using Caffeine. The caching configuration can be found in the [CaffeineCacheConfiguration](src/main/kotlin/com/example/templateproject/client/jsonplaceholder/JsonPlaceholderCacheConfiguration.kt) class.

## Thread pool

