package com.example.templateproject.web.configuration.filter

import com.example.templateproject.web.configuration.API_BASE_PATH
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.slf4j.MDC

@ExtendWith(MockKExtension::class)
internal class RequestIdFilterTest {

    private lateinit var victim: RequestIdFilter

    private val request = mockk<HttpServletRequest>()
    private val response = mockk<ServletResponse>()
    private val filterChain = mockk<FilterChain>()

    @BeforeEach
    fun initialize() {
        victim = RequestIdFilter()
        mockkStatic(MDC::class)

        every { filterChain.doFilter(request, response) } returns mockk()
        every { request.requestURI }.returns("${API_BASE_PATH}/test")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(MDC::class)
        MDC.clear()
    }

    @Test
    fun `Should not run logic if request is not HttpServletRequest`() {
        // given
        val request = mockk<ServletRequest>()

        every { filterChain.doFilter(request, response) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { MDC.put(any(), any()) }
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
        verify(exactly = 0) { MDC.put(any(), any()) }
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `Should use request id from header`() {
        // given
        val response = mockk<HttpServletResponse>()
        every { filterChain.doFilter(request, response) } returns mockk()

        every { request.getHeader(RequestIdFilter.REQUEST_ID_HEADER) }.returns("external_requestId")
        every { response.addHeader(any(), any()) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verifySequence {
            MDC.put(RequestIdFilter.REQUEST_ID_MDC_KEY, any())
            response.addHeader(RequestIdFilter.REQUEST_ID_HEADER, any())
            MDC.remove(RequestIdFilter.REQUEST_ID_MDC_KEY)
        }
    }

    @Test
    fun `Should generate request id if header contains empty id`() {
        // given
        val response = mockk<HttpServletResponse>()
        every { filterChain.doFilter(request, response) } returns mockk()

        every { request.getHeader(RequestIdFilter.REQUEST_ID_HEADER) }.returns("")
        every { response.addHeader(any(), any()) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verifySequence {
            MDC.put(RequestIdFilter.REQUEST_ID_MDC_KEY, any())
            response.addHeader(RequestIdFilter.REQUEST_ID_HEADER, any())
            MDC.remove(RequestIdFilter.REQUEST_ID_MDC_KEY)
        }
    }

    @Test
    fun `Should generate request id`() {
        // given
        val response = mockk<HttpServletResponse>()
        every { filterChain.doFilter(request, response) } returns mockk()

        every { request.getHeader(RequestIdFilter.REQUEST_ID_HEADER) }.returns(null)
        every { response.addHeader(any(), any()) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verifySequence {
            MDC.put(RequestIdFilter.REQUEST_ID_MDC_KEY, any())
            response.addHeader(RequestIdFilter.REQUEST_ID_HEADER, any())
            MDC.remove(RequestIdFilter.REQUEST_ID_MDC_KEY)
        }
    }
}
