package com.example.exception

import com.example.entity.Example
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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

    private lateinit var victim: ExampleExceptionHandler

    @BeforeEach
    fun initialize() {
        victim = ExampleExceptionHandler(true)
        every { webRequest.getParameterValues("trace") } returns Array(1) { "false" }
    }

    @Test
    fun `Should generate response entity from known exception`() {
        // given
        val id = 10L
        val exception = IdNotFoundException(Example::class, id)
        val exceptionIdString = "com.example.exception.IdNotFoundException" +
                "com.example.exception.ExampleExceptionHandlerTest" +
                "Should generate response entity from known exception" +
                35
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
                56
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
                79
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
                99
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
}
