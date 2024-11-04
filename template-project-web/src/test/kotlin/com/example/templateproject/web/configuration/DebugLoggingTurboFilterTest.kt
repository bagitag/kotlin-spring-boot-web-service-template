package com.example.templateproject.web.configuration

import ch.qos.logback.classic.Logger
import ch.qos.logback.core.spi.FilterReply
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.slf4j.MDC

@ExtendWith(MockKExtension::class)
internal class DebugLoggingTurboFilterTest {

    private lateinit var victim: DebugLoggingTurboFilter

    @BeforeEach
    fun initialize() {
        victim = DebugLoggingTurboFilter()
        victim.setOnMatch("ACCEPT")
    }

    @AfterEach
    fun tearDown() {
        MDC.remove("key")
    }

    @Test
    fun `Should return NEUTRAL if Value is missing`() {
        // given
        victim.start()

        // when
        val actual = victim.decide(null, null, null, null, null, null)

        // then
        assertEquals(FilterReply.NEUTRAL, actual)
    }

    @Test
    fun `Should return NEUTRAL if MDCKey is missing`() {
        // given
        victim.setValue("value")
        victim.start()

        // when
        val actual = victim.decide(null, null, null, null, null, null)

        // then
        assertEquals(FilterReply.NEUTRAL, actual)
    }

    @Test
    fun `Should return NEUTRAL if MDC is empty`() {
        // given
        victim.setValue("value")
        victim.setMDCKey("key")
        victim.start()

        val logger: Logger = Mockito.mock(Logger::class.java)
        `when`(logger.name).thenAnswer { "com.example.ClassName" }

        // when
        val actual = victim.decide(null, logger, null, null, null, null)

        // then
        assertEquals(FilterReply.NEUTRAL, actual)
    }

    @Test
    fun `Should return NEUTRAL if MDC contains key, but value does not match`() {
        // given
        val value = "value"
        val key = "key"
        victim.setValue(value)
        victim.setMDCKey(key)
        victim.start()

        MDC.put(key, "not_$value")

        val logger: Logger = Mockito.mock(Logger::class.java)
        `when`(logger.name).thenAnswer { "com.example.ClassName" }

        // when
        val actual = victim.decide(null, logger, null, null, null, null)

        // then
        assertEquals(FilterReply.NEUTRAL, actual)
    }

    @Test
    fun `Should return NEUTRAL if MDC is ok but logger is null`() {
        // given
        val value = "value"
        val key = "key"
        victim.setValue(value)
        victim.setMDCKey(key)
        victim.start()

        MDC.put(key, value)

        // and
        victim.setPackages("com.my.package")

        // when
        val actual = victim.decide(null, null, null, null, null, null)

        // then
        assertEquals(FilterReply.NEUTRAL, actual)
    }

    @Test
    fun `Should return NEUTRAL if MDC is ok but logger name is null`() {
        // given
        val value = "value"
        val key = "key"
        victim.setValue(value)
        victim.setMDCKey(key)
        victim.start()

        MDC.put(key, value)

        val logger: Logger = Mockito.mock(Logger::class.java)
        `when`(logger.name).thenAnswer { null }

        // and
        victim.setPackages("com.my.package")

        // when
        val actual = victim.decide(null, logger, null, null, null, null)

        // then
        assertEquals(FilterReply.NEUTRAL, actual)
    }

    @Test
    fun `Should return NEUTRAL if MDC is ok but package does not match`() {
        // given
        val value = "value"
        val key = "key"
        victim.setValue(value)
        victim.setMDCKey(key)
        victim.start()

        MDC.put(key, value)

        // and
        victim.setPackages("com.my.package")

        val logger: Logger = Mockito.mock(Logger::class.java)
        `when`(logger.name).thenAnswer { "com.other.package.ClassName" }

        // when
        val actual = victim.decide(null, logger, null, null, null, null)

        // then
        assertEquals(FilterReply.NEUTRAL, actual)
    }

    @Test
    fun `Should return ACCEPT for project base package`() {
        // given
        val value = "value"
        val key = "key"
        victim.setValue(value)
        victim.setMDCKey(key)
        victim.start()

        MDC.put(key, value)

        val logger: Logger = Mockito.mock(Logger::class.java)
        `when`(logger.name).thenAnswer { "com.example.templateproject.ClassName" }

        // when
        val actual = victim.decide(null, logger, null, null, null, null)

        // then
        assertEquals(FilterReply.ACCEPT, actual)
    }

    @Test
    fun `Should return ACCEPT`() {
        // given
        val value = "value"
        val key = "key"
        victim.setValue(value)
        victim.setMDCKey(key)
        victim.start()

        MDC.put(key, value)

        // and
        victim.setPackages("com.my.package")

        val logger: Logger = Mockito.mock(Logger::class.java)
        `when`(logger.name).thenAnswer { "com.my.package.ClassName" }

        // when
        val actual = victim.decide(null, logger, null, null, null, null)

        // then
        assertEquals(FilterReply.ACCEPT, actual)
    }
}
