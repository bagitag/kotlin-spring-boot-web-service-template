package com.example.templateproject.web.configuration.filter

import com.example.templateproject.web.configuration.API_BASE_PATH
import io.micrometer.tracing.TraceContext
import io.micrometer.tracing.Tracer
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.DispatcherType
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.String

@ExtendWith(MockKExtension::class)
internal class TraceIdFilterTest {
    private lateinit var victim: TraceIdFilter

    private val tracer = mockk<Tracer>()
    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val filterChain = mockk<FilterChain>()

    @BeforeEach
    fun initialize() {
        victim = TraceIdFilter(tracer)

        every { request.requestURI }.returns("${API_BASE_PATH}/test")
        every { filterChain.doFilter(request, response) } returns mockk()

        every { request.dispatcherType } returns DispatcherType.REQUEST
        every { request.getAttribute(any()) } returns null
    }

    @AfterEach
    fun tearDown() {
        verify(exactly = 1) { filterChain.doFilter(request, response) }
    }

    @ParameterizedTest
    @CsvSource("/actuator", "/swagger-ui", "/api-docs")
    fun `Should not run logic if request path is disabled`(input: String) {
        // given
        every { request.requestURI }.returns(input)

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { response.setHeader(TraceIdFilter.TRACE_ID_HEADER, any()) }
    }

    @Test
    fun `Should not run logic if the current trace context is null`() {
        // given
        every { tracer.currentTraceContext().context() }.returns(null)

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { response.setHeader(TraceIdFilter.TRACE_ID_HEADER, any()) }
    }

    @Test
    fun `Should set trace id as response header`() {
        // given
        val traceContext = mockk<TraceContext>()
        every { tracer.currentTraceContext().context() }.returns(traceContext)

        val traceId = "traceId"
        every { traceContext.traceId() }.returns(traceId)

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 1) { response.setHeader(TraceIdFilter.TRACE_ID_HEADER, traceId) }
    }
}
