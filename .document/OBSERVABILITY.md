# Observability

<details>
  <summary>Table of Contents</summary>

- [Logging](#logging)
- [Metrics](#metrics)
- [Tracing](#tracing)

</details>

The project is configured to support observability features with the help of the following technologies:
- [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/)
- [OpenTelemetry](https://opentelemetry.io/)
- [Prometheus](https://prometheus.io/)
- [Grafana](https://grafana.com/)
- [Loki](https://grafana.com/oss/loki/)
- [Tempo](https://grafana.com/oss/tempo/)


## Logging

The project uses the Spring Boot default, [Logback](https://logback.qos.ch/) as the logging framework. The configuration is defined in the [logback-spring.xml](../template-project-web/src/main/resources/logback-spring.xml) file.

### Development mode

- If the application is running in the development mode (`dev` profile) Spring's default configuration is used for console logging.
- For a few packages, the logging level is set to `DEBUG` in the [application-dev.properties](../template-project-web/src/main/resources/application-dev.properties) file.
- `requestId` is logged for each request. See the `Request ID generation` section of the [FEATURES.md](FEATURES.md) file for more details.
- OpenTelemetry is disabled.

### Non-dev mode

- Any other profile than `dev` uses Spring's default configuration for structured console logging in Logstash format.
- Request correlation: HTTP requests are logged with request identifiers (trace and span IDs are included when tracing is enabled).
- The trace ID is included in the response headers as `X-Trace-ID` with the help of the [TraceIdFilter](../template-project-web/src/main/kotlin/com/example/templateproject/web/configuration/filter/TraceIdFilter.kt).
- The OpenTelemetryAppender is configured to forward logs to the OpenTelemetry Collector.
- Grafana Loki is used for log aggregation and querying.

## Metrics

The default configuration of the Spring Boot Actuator is used for metrics collection. That means metrics like JVM memory, CPU usage, HTTP communication, etc. are collected automatically.

### Development mode

- Metrics are exposed on the `/actuator/metrics` endpoint in every profile.
- Micrometer's Prometheus registry and the `/actuator/prometheus` endpoint are enabled.
- Exporting metrics to OpenTelemetry is disabled.

### Non-dev mode

- Both the `/actuator/metrics` and `/actuator/prometheus` endpoints are disabled.
- OpenTelemetry is configured for metrics collection, it exports metrics to Prometheus.
- Grafana is used for metrics visualization.
- The configuration for the observability stack, which is used by the `docker` Spring profile, is defined in the [docker-compose-dev.yml](../docker-compose-dev.yml) file.

### Custom metrics

The project introduces a custom metric called `app.method.executions`. It is used to measure the time of method executions with the help of the `@Timed` annotation. See the example in the [ExampleMapper](../template-project-core/src/main/kotlin/com/example/templateproject/core/mapper/ExampleMapper.kt) class.

Percentile histograms are configured for the `http.server.requests`, `http.client.requests`, and `app.method.executions` metrics.

SLO histogram buckets are also configured for the `http.server.requests` and `http.client.requests` metrics.

### Dashboards

Dashboards for Grafana can be found in the `.docker/grafana/dashboards` directory.

## Tracing

The project uses Micrometer Tracing as the Spring Boot abstraction layer for distributed tracing.
Traces are exported to the OpenTelemetry Collector, which forwards them to Grafana Tempo for storage and analysis.

### Development mode

- OpenTelemetry and trace collection are disabled via the `management.otlp.tracing.export.enabled` property in the [application-dev.properties](../template-project-web/src/main/resources/application-dev.properties) file.
- Low level tracing is possible with the help of the `requestId`. See the `Request ID generation` section of the [FEATURES.md](FEATURES.md) file for more details.

### Non-dev mode

- The `requestId` handling functionality is disabled.
- OpenTelemetry is enabled and traces are forwarded to Grafana Tempo.
- `traceId` is included in the response headers as `X-Trace-ID` with the help of the [TraceIdFilter](../template-project-web/src/main/kotlin/com/example/templateproject/web/configuration/filter/TraceIdFilter.kt).
- `traceparent` header are added to the HTTP requests.

### Custom spans

Spans can be added to the trace with the help of `@Span` and `@SpanTag` annotations. See the example in the [ExampleMapper](../template-project-core/src/main/kotlin/com/example/templateproject/core/mapper/ExampleMapper.kt) class.

### Trace propagation

The project uses the `traceparent` header for trace propagation, it allows the trace to be continued on the application side.  
The `traceparent` header is also passed along with the request to the external services.
