package com.example.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class ExceptionMetrics(private val meterRegistry: MeterRegistry) {

    private companion object {
        const val exceptionCounterMetricName = "app.exception.counter"
        const val exceptionIdTagName = "exceptionId"
        const val exceptionTypeTagName = "exceptionType"
    }

    fun updateExceptionCounter(exceptionId: String, exceptionClass: String) {
        Counter.builder(exceptionCounterMetricName)
            .description("Represents the exception count grouped by the generated id.")
            .tag(exceptionIdTagName, exceptionId)
            .tag(exceptionTypeTagName, exceptionClass)
            .register(meterRegistry)
            .increment()
    }
}
