# Observability

This document describes the observability features of the project.

---

<details>
  <summary>Table of Contents</summary>

- [Logging](#logging)
- [Metrics](#metrics)
- [Tracing](#tracing)

</details>

The project supports observability with the help of the following technologies:
- [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/)
- [OpenTelemetry](https://opentelemetry.io/)
- [Prometheus](https://prometheus.io/)
- [Grafana](https://grafana.com/)
- [Loki](https://grafana.com/oss/loki/)
- [Tempo](https://grafana.com/oss/tempo/)


## Logging

The project uses the Spring Boot default logging framework [Logback](https://logback.qos.ch/). The configuration is in [logback-spring.xml](../template-project-web/src/main/resources/logback-spring.xml).

### Development mode

- When running in development mode (`dev` profile), Spring's default configuration is used for console logging.
- The logging level for a few packages is set to `DEBUG` in [application-dev.properties](../template-project-web/src/main/resources/application-dev.properties).
- `requestId` is logged for each request. See the `Request ID generation` section of [FEATURES.md](FEATURES.md) for details.
- OpenTelemetry is disabled.

### Non-dev mode

- Profiles other than `dev` uses Spring's default configuration for structured console logging in Logstash format.
- Request correlation: HTTP requests are logged with request identifiers (trace and span IDs are included when tracing is enabled).
- The trace ID is included in the response headers as `X-Trace-ID` via [TraceIdFilter](../template-project-web/src/main/kotlin/com/example/templateproject/web/configuration/filter/TraceIdFilter.kt).
- The OpenTelemetryAppender forwards logs to the OpenTelemetry Collector.
- Grafana Loki is used for log aggregation and querying.

## Metrics

Spring Boot Actuator's default configuration is used for metrics collection. Metrics such as JVM memory, CPU usage, HTTP activity are collected automatically.

### Development mode

- Metrics are exposed at `/actuator/metrics` in all profiles.
- Micrometer's Prometheus registry and the `/actuator/prometheus` endpoint are enabled.
- Exporting metrics to OpenTelemetry is disabled.

### Non-dev mode

- Both `/actuator/metrics` and `/actuator/prometheus` are disabled.
- OpenTelemetry collects metrics and exports them to Prometheus.
- Grafana is used for visualization.
- For the `docker` Spring profile the observability stack configuration is defined in the [docker-compose-dev.yml](../docker-compose-dev.yml) file.

### Custom metrics

The project introduces a custom metric called `app.method.executions` to measure method execution time via the `@Timed` annotation. See [ExampleMapper](../template-project-core/src/main/kotlin/com/example/templateproject/core/mapper/ExampleMapper.kt) class for an example.

Percentile histograms are configured for `http.server.requests`, `http.client.requests`, and `app.method.executions` metrics.

SLO histogram buckets are also configured for `http.server.requests` and `http.client.requests` metrics.

### Dashboards

Dashboards for Grafana can be found in the `.docker/grafana/dashboards` directory.

## Tracing

The project uses Micrometer Tracing as the Spring Boot abstraction layer for distributed tracing.
Traces are exported to the OpenTelemetry Collector, which forwards them to Grafana Tempo for storage and analysis.

### Development mode

- OpenTelemetry and trace collection are disabled via the `management.otlp.tracing.export.enabled` property in [application-dev.properties](../template-project-web/src/main/resources/application-dev.properties).
- Low-level tracing is supported with the help of the `requestId`. See the `Request ID generation` section of [FEATURES.md](FEATURES.md) for details.

### Non-dev mode

- The `requestId` handling functionality is disabled.
- OpenTelemetry is enabled and traces are forwarded to Grafana Tempo.
- The `traceId` is included in the response headers as `X-Trace-ID` via [TraceIdFilter](../template-project-web/src/main/kotlin/com/example/templateproject/web/configuration/filter/TraceIdFilter.kt).
- The `traceparent` header are added to outgoing HTTP requests.

### Custom spans

Spans can be added to the trace with the help of `@Span` and `@SpanTag` annotations. See [ExampleMapper](../template-project-core/src/main/kotlin/com/example/templateproject/core/mapper/ExampleMapper.kt) for an example.

### Trace propagation

The `traceparent` header can be used for trace propagation. The header is also forwarded to external services.
