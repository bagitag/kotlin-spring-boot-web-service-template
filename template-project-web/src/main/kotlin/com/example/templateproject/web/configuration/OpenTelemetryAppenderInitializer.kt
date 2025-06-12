package com.example.templateproject.web.configuration

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenTelemetryAppenderInitializer(private val openTelemetry: OpenTelemetry) : InitializingBean {

    override fun afterPropertiesSet() {
        OpenTelemetryAppender.install(openTelemetry)
    }
}
