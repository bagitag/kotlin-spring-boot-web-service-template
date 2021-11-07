package com.example.exception

import com.example.dto.ErrorDTO
import com.example.entity.Example
import com.example.metrics.ExceptionMetrics
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.util.DigestUtils
import org.springframework.web.HttpMediaTypeNotSupportedException
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
        every { webRequest.getParameterValues("trace") } returns emptyArray()
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
                46
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        assertEquals("Could not find Example with id: $id", actual.body!!.message)
        assertEquals(expected, actual.body!!.id)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity from known exception with stack trace`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.exception.IdNotFoundException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from known exception with stack trace" +
                68
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // and
        every { webRequest.getParameterValues("trace") } returns Array(1) { "true" }

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        assertEquals("Could not find Example with id: $id", actual.body!!.message)
        assertEquals(expected, actual.body!!.id)
        assertFalse(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity without stacktrace if the feature is disabled`() {
        // given
        victim = ExampleExceptionHandler(false, exceptionMetrics)

        val id = 10L
        val exception = IdNotFoundException(Example::class, id)

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity without stacktrace if the trace request parameter is null`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)

        // and
        every { webRequest.getParameterValues("trace") } returns null

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity without stacktrace if the trace request parameter is not valid`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)

        // and
        every { webRequest.getParameterValues("trace") } returns arrayOf(null, "", "   ", "asdas", "false")

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity from exception`() {
        // given
        val exception = RuntimeException()
        val exceptionIdString = "java.lang.RuntimeException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from exception" +
                144
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.statusCode)
        assertEquals(ExampleExceptionHandler.unknownError, actual.body!!.message)
        assertEquals(expected, actual.body!!.id)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity from exception with stack trace`() {
        // given
        val exception = RuntimeException()
        val exceptionIdString = "java.lang.RuntimeException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from exception with stack trace" +
                164
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // and
        every { webRequest.getParameterValues("trace") } returns Array(1) { "true" }

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        assertEquals(ExampleExceptionHandler.unknownError, actual.body!!.message)
        assertEquals(expected, actual.body!!.id)
        assertFalse(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate exception id and update the related counter`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.exception.IdNotFoundException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate exception id and update the related counter" +
                189
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        // and
        every { exceptionMetrics.updateExceptionCounter(expected) } returns Unit

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        assertEquals("Could not find Example with id: $id", actual.body!!.message)
        assertEquals(expected, actual.body!!.id)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
        verify { exceptionMetrics.updateExceptionCounter(expected) }
    }

    @Test
    fun `Should generate response entity with unknown exception id`() {
        // given
        val exception = RuntimeException()
        mockkStatic(StackWalker::class)
        every { StackWalker.getInstance() } throws ClassNotFoundException()

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        assertEquals(ExampleExceptionHandler.unknownError, actual.body!!.message)
        assertEquals("unknown", actual.body!!.id)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())

        unmockkStatic(StackWalker::class)
    }

    @Test
    fun `Should generate response entity from internal exceptions`() {
        // given
        val message = "unsupported"
        val exception = HttpMediaTypeNotSupportedException(message)
        val exceptionIdString = "org.springframework.web.HttpMediaTypeNotSupportedException" +
                "com.external.ExampleClass" +
                "exampleMethod" +
                100
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(10)

        exception.stackTrace = arrayOf(
            StackTraceElement("com.external.ExampleClass", "exampleMethod", "exampleFile", 100))

        // when
        val actual = victim.handleExceptionInternal(exception, null, HttpHeaders(),
            HttpStatus.UNSUPPORTED_MEDIA_TYPE, webRequest)

        // then
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, actual.statusCode)
        assertTrue(actual.body is ErrorDTO)
        val body = actual.body as ErrorDTO
        assertEquals(message, body.message)
        assertEquals(expected, body.id)
        assertTrue(body.stackTrace.isNullOrEmpty())
    }
}
