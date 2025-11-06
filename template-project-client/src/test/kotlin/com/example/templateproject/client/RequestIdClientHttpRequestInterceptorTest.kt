package com.example.templateproject.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse

class RequestIdClientHttpRequestInterceptorTest {
    private lateinit var victim: RequestIdClientHttpRequestInterceptor
    private val execution = mockk<ClientHttpRequestExecution>()
    private val response = mockk<ClientHttpResponse>()
    private val request = mockk<HttpRequest>()

    @BeforeEach
    fun initialize() {
        victim = RequestIdClientHttpRequestInterceptor()
        every { request.headers } returns HttpHeaders()
        every { execution.execute(any(), any()) } returns response
    }

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    @Test
    fun `Should not add header when requestId missing`() {
        // when
        val actual = victim.intercept(request, byteArrayOf(), execution)

        // then
        assertEquals(response, actual)
        assertNull(request.headers.getFirst("X-Request-ID"))
        verify { execution.execute(request, byteArrayOf()) }
    }

    @Test
    fun `Should add X-Request-ID when requestId present`() {
        // given
        val requestId = "xyz-789"
        MDC.put("requestId", requestId)

        // when
        val actual = victim.intercept(request, byteArrayOf(), execution)

        // then
        assertEquals(response, actual)
        assertEquals(requestId, request.headers.getFirst("X-Request-ID"))
        verify { execution.execute(request, byteArrayOf()) }
    }
}
