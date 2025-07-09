package com.example.templateproject.web.configuration.filter

import com.example.templateproject.web.configuration.API_BASE_PATH
import io.micrometer.tracing.TraceContext
import io.micrometer.tracing.Tracer
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(MockKExtension::class)
internal class TraceIdFilterTest {

    private lateinit var victim: TraceIdFilter

    private val tracer = mockk<Tracer>()
    private val request = mockk<HttpServletRequest>()
    private val response = mockk<HttpServletResponse>()
    private val filterChain = mockk<FilterChain>()

    @BeforeEach
    fun initialize() {
        victim = TraceIdFilter(tracer)

        every { filterChain.doFilter(request, response) } returns mockk()
        every { request.requestURI }.returns("${API_BASE_PATH}/test")
    }

    @Test
    fun `Should not run logic if request is not HttpServletRequest`() {
        // given
        val request = mockk<ServletRequest>()

        every { filterChain.doFilter(request, response) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `Should not run logic if response is not HttpServletResponse`() {
        // given
        val response = mockk<ServletResponse>()

        every { filterChain.doFilter(request, response) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @ParameterizedTest
    @CsvSource("/actuator", "/swagger-ui", "/api-docs")
    fun `Should not run logic if request path is disabled`(input: String) {
        // given
        every { request.requestURI }.returns(input)

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `Should not run logic if the current trace context is null`() {
        // given
        every { tracer.currentTraceContext().context() }.returns(null)

        every { filterChain.doFilter(request, response) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { response.addHeader(TraceIdFilter.TRACE_ID_HEADER, any()) }
    }

    @Test
    fun `Should add trace id as response header`() {
        // given
        val traceContext = mockk<TraceContext>()

        every { tracer.currentTraceContext().context() }.returns(traceContext)

        val traceId = "traceId"
        every { traceContext.traceId() }.returns(traceId)

        every { response.addHeader(TraceIdFilter.TRACE_ID_HEADER, traceId) } returns mockk()

        every { filterChain.doFilter(request, response) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 1) { response.addHeader(TraceIdFilter.TRACE_ID_HEADER, traceId) }
    }
}
