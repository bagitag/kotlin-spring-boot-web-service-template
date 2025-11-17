package com.example.templateproject.web.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class ExceptionMetrics(
    private val meterRegistry: MeterRegistry,
) {
    private companion object {
        const val EXCEPTION_COUNTER_METRIC_NAME = "app.exception.counter"
        const val EXCEPTION_ID_TAG_NAME = "exceptionId"
        const val EXCEPTION_TYPE_TAG_NAME = "exceptionType"
    }

    fun updateExceptionCounter(
        exceptionId: String,
        exceptionClass: String,
    ) {
        Counter
            .builder(EXCEPTION_COUNTER_METRIC_NAME)
            .description("Represents the exception count grouped by the generated id.")
            .tag(EXCEPTION_ID_TAG_NAME, exceptionId)
            .tag(EXCEPTION_TYPE_TAG_NAME, exceptionClass)
            .register(meterRegistry)
            .increment()
    }
}
