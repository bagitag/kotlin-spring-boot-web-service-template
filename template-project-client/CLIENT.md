# Client module

This module is responsible for integrating with third-party services and external APIs.
[JSONPlaceholder](https://jsonplaceholder.typicode.com/) is used as an example.

## Configuration

The configuration can be found in the **client-\[spring-profile].yml** files.

### Docker profile

- It uses a [json-server](https://www.npmjs.com/package/json-server) as a mock service that is started in a Docker container based on the [docker-compose-dev.yml](../docker-compose-dev.yml) file.

## Features

- RestClient
- Caching with [Caffeine](https://github.com/ben-manes/caffeine)
- Spring Retry
- Circuit Breaker pattern 
