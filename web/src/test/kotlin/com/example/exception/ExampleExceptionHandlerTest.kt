package com.example.exception

import com.example.entity.Example
import com.example.metrics.ExceptionMetrics
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.util.DigestUtils
import org.springframework.web.context.request.WebRequest

@ExtendWith(MockKExtension::class)
internal class ExampleExceptionHandlerTest {

    @MockK
    private lateinit var webRequest: WebRequest
    @MockK
    private lateinit var exceptionMetrics: ExceptionMetrics

    private lateinit var victim: ExampleExceptionHandler

    @BeforeEach
    fun initialize() {
        victim = ExampleExceptionHandler(true, exceptionMetrics)
        every { webRequest.getParameterValues("trace") } returns Array(1) { "false" }
        every { exceptionMetrics.updateExceptionCounter(any()) } returns Unit
    }

    @Test
    fun `Should generate response entity from known exception`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.exception.IdNotFoundException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from known exception" +
                40
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // when
        val actual = victim.handleIdNotFoundException(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        assertEquals("Could not find Example with id: $id", actual.body.message)
        assertEquals(expected, actual.body.id)
        assertTrue(actual.body.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity from known exception with stack trace`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.exception.IdNotFoundException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from known exception with stack trace" +
                61
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // and
        every { webRequest.getParameterValues("trace") } returns Array(1) { "true" }

        // when
        val actual = victim.handleIdNotFoundException(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        assertEquals("Could not find Example with id: $id", actual.body.message)
        assertEquals(expected, actual.body.id)
        assertFalse(actual.body.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity from exception`() {
        // given
        val exception = RuntimeException()
        val exceptionIdString = "java.lang.RuntimeException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from exception" +
                84
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // when
        val actual = victim.handleIdNotFoundException(exception, webRequest)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.statusCode)
        assertEquals(ExampleExceptionHandler.unknownError, actual.body.message)
        assertEquals(expected, actual.body.id)
        assertTrue(actual.body.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity from exception with stack trace`() {
        // given
        val exception = RuntimeException()
        val exceptionIdString = "java.lang.RuntimeException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from exception with stack trace" +
                104
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // and
        every { webRequest.getParameterValues("trace") } returns Array(1) { "true" }

        // when
        val actual = victim.handleIdNotFoundException(exception, webRequest)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.statusCode)
        assertEquals(ExampleExceptionHandler.unknownError, actual.body.message)
        assertEquals(expected, actual.body.id)
        assertFalse(actual.body.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate exception id and update the related counter`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.exception.IdNotFoundException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate exception id and update the related counter" +
                128
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // and
        every { exceptionMetrics.updateExceptionCounter(expected) } returns Unit

        // when
        val actual = victim.handleIdNotFoundException(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        assertEquals("Could not find Example with id: $id", actual.body.message)
        assertEquals(expected, actual.body.id)
        assertTrue(actual.body.stackTrace.isNullOrEmpty())
        verify { exceptionMetrics.updateExceptionCounter(expected) }
    }
}
