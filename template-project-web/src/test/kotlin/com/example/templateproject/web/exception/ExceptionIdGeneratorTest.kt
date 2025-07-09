package com.example.templateproject.web.exception

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.util.DigestUtils
import org.springframework.web.HttpMediaTypeNotSupportedException

internal class ExceptionIdGeneratorTest {
    @Test
    fun `Should return unknown exception id`() {
        // given
        val exception = RuntimeException()
        mockkStatic(StackWalker::class)
        every { StackWalker.getInstance() } throws ClassNotFoundException()

        // when
        val actual = ExceptionIdGenerator.generateExceptionId(exception)

        // then
        assertEquals("unknown", actual)
        unmockkStatic(StackWalker::class)
    }

    @Test
    fun `Should generate response entity from external exception`() {
        // given
        val exception = Exception()

        exception.stackTrace =
            arrayOf(
                StackTraceElement("com.test.first.StackTraceElement1", "testMethod", "StackTraceElement1.java", 100),
                StackTraceElement("com.test.second.StackTraceElement2", "testMethod", "StackTraceElement2.java", 100),
            )

        val exceptionIdString = "java.lang.Exception" + "com.test.first.StackTraceElement1" + "testMethod"
        val expected =
            DigestUtils
                .md5DigestAsHex(exceptionIdString.toByteArray())
                .take(ExceptionIdGenerator.EXCEPTION_ID_LENGTH)

        // when
        val actual = ExceptionIdGenerator.generateExceptionId(exception)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun `Should generate response entity from exception`() {
        // given
        val exception = RuntimeException()
        val exceptionIdString =
            "java.lang.RuntimeException" +
                "com.example.templateproject.web.exception.ExceptionIdGeneratorTest" +
                "Should generate response entity from exception"
        val expected =
            DigestUtils
                .md5DigestAsHex(exceptionIdString.toByteArray())
                .take(ExceptionIdGenerator.EXCEPTION_ID_LENGTH)

        // when
        val actual = ExceptionIdGenerator.generateExceptionId(exception)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun `Should generate response entity from internal exceptions`() {
        // given
        val exception = HttpMediaTypeNotSupportedException("unsupported")
        val exceptionIdString =
            "org.springframework.web.HttpMediaTypeNotSupportedException" +
                "com.example.templateproject.web.exception.ExceptionIdGeneratorTest" +
                "Should generate response entity from internal exceptions"
        val expected =
            DigestUtils
                .md5DigestAsHex(exceptionIdString.toByteArray())
                .take(ExceptionIdGenerator.EXCEPTION_ID_LENGTH)

        exception.stackTrace =
            arrayOf(
                StackTraceElement("com.test.first.StackTraceElement1", "testMethod", "StackTraceElement1.java", 100),
                StackTraceElement("com.test.second.StackTraceElement2", "testMethod", "StackTraceElement2.java", 200),
                StackTraceElement(
                    "com.example.templateproject.web.exception.ExceptionIdGeneratorTest",
                    "Should generate response entity from internal exceptions",
                    "ExceptionIdGeneratorTest.kt",
                    99,
                ),
                StackTraceElement("com.test.third.StackTraceElement3", "testMethod", "StackTraceElement3.java", 300),
            )

        // when
        val actual = ExceptionIdGenerator.generateExceptionId(exception)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun `Should not generate exception id from excluded classes`() {
        // given
        val exception = NullPointerException()

        exception.stackTrace =
            arrayOf(
                StackTraceElement("com.test.first.StackTraceElement1", "testMethod", "StackTraceElement1.java", 100),
                StackTraceElement("com.test.second.StackTraceElement2", "testMethod", "StackTraceElement2.java", 200),
                StackTraceElement(
                    "com.example.templateproject.web.configuration.filter.DebugHeaderFilter",
                    "doFilter",
                    "DebugHeaderFilter.kt",
                    45,
                ),
                StackTraceElement("com.test.third.StackTraceElement3", "testMethod", "StackTraceElement3.java", 300),
            )

        val exceptionIdString =
            "java.lang.NullPointerException" +
                "com.test.first.StackTraceElement1" +
                "testMethod"
        val expected =
            DigestUtils
                .md5DigestAsHex(exceptionIdString.toByteArray())
                .take(ExceptionIdGenerator.EXCEPTION_ID_LENGTH)

        // when
        val actual = ExceptionIdGenerator.generateExceptionId(exception)

        // then
        assertEquals(expected, actual)
    }
}
