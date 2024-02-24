package com.example.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ExceptionMetricsTest {

    @RelaxedMockK
    private lateinit var meterRegistry: MeterRegistry
    @MockK
    private lateinit var counter: Counter

    private lateinit var victim: ExceptionMetrics

    @BeforeEach
    fun initialize() {
        victim = ExceptionMetrics(meterRegistry)
    }

    @Test
    fun `Should increment counter`() {
        // given
        val exceptionId = "hs7sm2df"
        val exceptionClass = "java.lang.NullPointerException"

        every { Counter.builder("app.exception.counter")
            .description("Represents the exception count grouped by the generated id.")
            .tag("exceptionId", exceptionId)
            .tag("exceptionType", exceptionClass)
            .register(meterRegistry)
        } returns counter
        every { counter.increment() } returns Unit

        // when
        victim.updateExceptionCounter(exceptionId, exceptionClass)

        // then
        verify { counter.increment() }
    }
}
