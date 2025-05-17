package com.example.templateproject.web.configuration.filter

import com.example.templateproject.web.configuration.API_BASE_PATH
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
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
import org.slf4j.MDC

@ExtendWith(MockKExtension::class)
internal class DebugHeaderFilterTest {

    private lateinit var victim: DebugHeaderFilter

    private val request = mockk<HttpServletRequest>()
    private val response = mockk<ServletResponse>()
    private val filterChain = mockk<FilterChain>()

    @BeforeEach
    fun initialize() {
        victim = DebugHeaderFilter()
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

    @Test
    fun `Should not run logic if request path is not valid`() {
        // given
        every { request.requestURI }.returns("/actuator/info")

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { MDC.put(any(), any()) }
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `Should not run logic if debug header is null`() {
        // given
        every { request.getHeader(DebugHeaderFilter.DEBUG_REQUEST_HEADER_NAME) }.returns(null)

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { MDC.put(any(), any()) }
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `Should not run logic if debug header is empty`() {
        // given
        every { request.getHeader(DebugHeaderFilter.DEBUG_REQUEST_HEADER_NAME) }.returns("")

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { MDC.put(any(), any()) }
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `Should not run logic if debug header value does not match`() {
        // given
        every { request.getHeader(DebugHeaderFilter.DEBUG_REQUEST_HEADER_NAME) }.returns("test")

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { MDC.put(any(), any()) }
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `Should run logic`() {
        // given
        val response = mockk<HttpServletResponse>()
        every { filterChain.doFilter(request, response) } returns mockk()

        every { request.getHeader(DebugHeaderFilter.DEBUG_REQUEST_HEADER_NAME) }
            .returns(DebugHeaderFilter.DEBUG_REQUEST_HEADER_VALUE)

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verifyOrder {
            MDC.put(DebugHeaderFilter.DEBUG_MODE_MDC_KEY, DebugHeaderFilter.DEBUG_MODE_MDC_VALUE)
            MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY)
            MDC.remove(DebugHeaderFilter.DEBUG_MODE_MDC_KEY)
        }
    }
}
