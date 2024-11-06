package com.example.templateproject.web.exception

import com.example.templateproject.api.dto.ErrorDTO
import com.example.templateproject.client.exception.ExternalServiceException
import com.example.templateproject.core.exception.IdNotFoundException
import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.web.metrics.ExceptionMetrics
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.util.DigestUtils
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.WebRequest
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutionException

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
        every { exceptionMetrics.updateExceptionCounter(any(), any()) } returns Unit
    }

    @Test
    fun `Should generate response entity from known exception`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.templateproject.core.exception.IdNotFoundException" +
                "com.example.templateproject.web.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from known exception"
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray())
            .take(ExampleExceptionHandler.exceptionIdLength)

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        val body = actual.body!!
        assertEquals("Could not find Example with id: $id", body.message)
        assertEquals(expected, body.id)
        assertTrue(body.stackTrace.isNullOrEmpty())
        assertNull(body.details)
    }

    @Test
    fun `Should generate response entity from known exception with stack trace`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.templateproject.core.exception.IdNotFoundException" +
                "com.example.templateproject.web.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from known exception with stack trace"
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray())
            .take(ExampleExceptionHandler.exceptionIdLength)

        // and
        every { webRequest.getParameterValues("trace") } returns Array(1) { "true" }

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        val body = actual.body!!
        assertEquals("Could not find Example with id: $id", body.message)
        assertEquals(expected, body.id)
        assertFalse(body.stackTrace.isNullOrEmpty())
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
        assertNull(actual.body!!.details)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity from exception`() {
        // given
        val exception = RuntimeException()
        val exceptionIdString = "java.lang.RuntimeException" +
                "com.example.templateproject.web.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from exception"
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray())
            .take(ExampleExceptionHandler.exceptionIdLength)

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        val body = actual.body!!
        assertEquals(ExampleExceptionHandler.unknownError, body.message)
        assertEquals(expected, body.id)
        assertNull(body.details)
        assertTrue(body.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate response entity from exception with stack trace`() {
        // given
        val exception = RuntimeException()
        val exceptionIdString = "java.lang.RuntimeException" +
                "com.example.templateproject.web.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from exception with stack trace"
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray())
            .take(ExampleExceptionHandler.exceptionIdLength)

        // and
        every { webRequest.getParameterValues("trace") } returns Array(1) { "true" }

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        val body = actual.body!!
        assertEquals(ExampleExceptionHandler.unknownError, body.message)
        assertEquals(expected, body.id)
        assertFalse(body.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should generate exception id and update the related counter`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.templateproject.core.exception.IdNotFoundException" +
                "com.example.templateproject.web.exception.ExampleExceptionHandlerTest" +
                "Should generate exception id and update the related counter"
        val expectedId = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray())
            .take(ExampleExceptionHandler.exceptionIdLength)
        val expectedMessage = "Could not find Example with id: $id"

        // and
        every { exceptionMetrics.updateExceptionCounter(expectedId, exception.javaClass.name) } returns Unit

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        assertEquals(HttpStatus.NOT_FOUND, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        assertEquals(expectedMessage, actual.body!!.message)
        assertEquals(expectedId, actual.body!!.id)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
        verify { exceptionMetrics.updateExceptionCounter(expectedId, exception.javaClass.simpleName) }
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
                "exampleMethod"
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray())
            .take(ExampleExceptionHandler.exceptionIdLength)

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

    @Test
    fun `Should generate response entity with details`() {
        // given
        val errors = mutableListOf(
            FieldError("objectName", "field1", "errorMsg1"),
            FieldError("objectName", "field1", "errorMsg2"),
            FieldError("objectName", "field2", "errorMsg1")
        )

        val bindingResult: BindingResult = mock(BindingResult::class.java)
        `when`(bindingResult.allErrors).thenAnswer { errors }

        val parameter = mockk<MethodParameter> {
            every { parameterIndex } returns 1
        }
        val exception = MethodArgumentNotValidException(parameter, bindingResult)

        exception.stackTrace =
            arrayOf(StackTraceElement("com.external.ExampleClass",
                "exampleMethod",
                "exampleFile", 100))

        every { exception.message } returns "MethodArgumentNotValidException"

        val exceptionIdString = "org.springframework.web.bind.MethodArgumentNotValidException" +
                "com.external.ExampleClass" +
                "exampleMethod"
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray())
            .take(ExampleExceptionHandler.exceptionIdLength)

        every { webRequest.getParameterValues("trace") } returns null

        // when
        val actual = victim.handleExceptions(exception, webRequest)

        // then
        val body = actual.body as ErrorDTO
        val actualDetails = body.details as Map<String, List<String>>
        assertEquals(HttpStatus.BAD_REQUEST, actual.statusCode)
        assertEquals("Invalid request content.", body.message)
        assertEquals(2, actualDetails.size)
        assertEquals(2, actualDetails["field1"]!!.size)
        assertTrue(actualDetails["field1"]!!.contains("errorMsg1"))
        assertTrue(actualDetails["field1"]!!.contains("errorMsg2"))
        assertTrue(actualDetails["field2"]!!.contains("errorMsg1"))
        assertEquals(expected, body.id)
        assertTrue(actual.body!!.stackTrace.isNullOrEmpty())
    }

    @Test
    fun `Should unwrap ExecutionException`() {
        // given
        val errorMessage = "timeout error"
        val exception = ExternalServiceException(SocketTimeoutException(errorMessage), errorMessage, "MY-SERVICE")
        val originalException = ExecutionException(exception)
        val exceptionIdString = "com.example.templateproject.client.exception.ExternalServiceException" +
                "com.example.templateproject.web.exception.ExampleExceptionHandlerTest" +
                "Should unwrap ExecutionException"
        val expected = DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray())
            .take(ExampleExceptionHandler.exceptionIdLength)

        // when
        val actual = victim.handleExceptions(originalException, webRequest)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.statusCode)
        Assertions.assertNotNull(actual.body)
        val body = actual.body!!
        assertEquals(errorMessage, body.message)
        assertEquals(expected, body.id)
        assertNull(body.details)
        assertTrue(body.stackTrace.isNullOrEmpty())
    }
}
