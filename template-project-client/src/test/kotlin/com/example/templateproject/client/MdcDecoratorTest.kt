package com.example.templateproject.client

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC

@ExtendWith(MockKExtension::class)
internal class MdcDecoratorTest {
    private lateinit var victim: MdcDecorator

    @BeforeEach
    fun initialize() {
        victim = MdcDecorator()
        mockkStatic(MDC::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(MDC::class)
    }

    @Test
    fun `Should set and clear MDC context map`() {
        // given
        val runnable = Runnable { println("Running task") }
        val contextMap = mapOf("key" to "value")

        every { MDC.getCopyOfContextMap() } returns contextMap

        // when
        val decoratedRunnable = victim.decorate(runnable)
        decoratedRunnable.run()

        // then
        verifyOrder {
            MDC.getCopyOfContextMap()
            MDC.setContextMap(contextMap)
            runnable.run()
            MDC.clear()
        }
    }

    @Test
    fun `Should clear MDC context map when no context is set`() {
        // given
        val runnable = Runnable { println("Running task") }

        every { MDC.getCopyOfContextMap() } returns null

        // when
        val decoratedRunnable = victim.decorate(runnable)
        decoratedRunnable.run()

        // then
        verifyOrder {
            MDC.getCopyOfContextMap()
            runnable.run()
            MDC.clear()
        }
    }
}
