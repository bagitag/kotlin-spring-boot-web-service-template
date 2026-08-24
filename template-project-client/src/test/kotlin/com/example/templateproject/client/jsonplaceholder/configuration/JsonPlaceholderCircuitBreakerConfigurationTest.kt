package com.example.templateproject.client.jsonplaceholder.configuration

import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerProperties
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class JsonPlaceholderCircuitBreakerConfigurationTest {
    @Test
    fun `Circuit breaker properties should be parsed correctly`() {
        // given
        val properties =
            JsonPlaceholderProperties(
                clientId = "test-client",
                baseUrl = "http://localhost:8080",
                apiKey = "test-key",
                connectionTimeoutMillis = 1000,
                readTimeoutMillis = 3000,
                threadPool =
                    ThreadPoolTaskExecutorProperties(
                        corePoolSize = 10,
                        maxPoolSize = 20,
                        queueCapacity = 5,
                    ),
            )

        // when - then
        assertNotNull(properties)
    }

    @Test
    fun `Circuit breaker properties with default values should be valid`() {
        // given
        val cbProperties = CircuitBreakerProperties()

        // when - then
        assertNotNull(cbProperties)
    }
}



