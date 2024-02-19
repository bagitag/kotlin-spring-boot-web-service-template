package com.example.configuration

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
import org.slf4j.MDC

@ExtendWith(MockKExtension::class)
internal class DebugHeaderFilterTest {

    private lateinit var victim: DebugHeaderFilter

    @BeforeEach
    fun initialize() {
        victim = DebugHeaderFilter()
        mockkStatic(MDC::class)
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
        val response = mockk<ServletResponse>()
        val filterChain = mockk<FilterChain>()

        every { filterChain.doFilter(request, response) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verify(exactly = 0) { MDC.put(any(), any()) }
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `Should not run logic if debug header is null`() {
        // given
        val request = mockk<HttpServletRequest>()
        val response = mockk<ServletResponse>()
        val filterChain = mockk<FilterChain>()

        every { filterChain.doFilter(request, response) } returns mockk()
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
        val request = mockk<HttpServletRequest>()
        val response = mockk<ServletResponse>()
        val filterChain = mockk<FilterChain>()

        every { filterChain.doFilter(request, response) } returns mockk()
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
        val request = mockk<HttpServletRequest>()
        val response = mockk<ServletResponse>()
        val filterChain = mockk<FilterChain>()

        every { filterChain.doFilter(request, response) } returns mockk()
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
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()
        val filterChain = mockk<FilterChain>()

        every { filterChain.doFilter(request, response) } returns mockk()
        every { request.getHeader(DebugHeaderFilter.DEBUG_REQUEST_HEADER_NAME) }
            .returns(DebugHeaderFilter.DEBUG_REQUEST_HEADER_VALUE)
        every { response.addHeader(any(), any()) } returns mockk()

        // when
        assertDoesNotThrow { victim.doFilter(request, response, filterChain) }

        // then
        verifySequence {
            MDC.put(DebugHeaderFilter.MDC_KEY, DebugHeaderFilter.MDC_VALUE)
            MDC.put(DebugHeaderFilter.REQUEST_ID, any())
            response.addHeader(DebugHeaderFilter.REQUEST_ID, any())
            MDC.remove(DebugHeaderFilter.MDC_KEY)
        }
    }
}
