package com.example.templateproject.client.jsonplaceholder.configuration

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

//@Configuration
class JsonPlaceholderCircuitBreakerConfiguration {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(JsonPlaceholderCircuitBreakerConfiguration::class.java)
        private const val JSONPLACEHOLDER_CIRCUIT_BREAKER_NAME = "jsonplaceholder"
        private const val DEFAULT_FAILURE_RATE_THRESHOLD = 50f
        private const val DEFAULT_SLOW_CALL_RATE_THRESHOLD = 50f
        private const val DEFAULT_SLOW_CALL_DURATION_THRESHOLD_SECONDS = 2L
        private const val DEFAULT_WAIT_DURATION_IN_OPEN_STATE_SECONDS = 5L
        private const val DEFAULT_PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE = 3
    }

    @Bean
    fun jsonPlaceholderCircuitBreaker(
        circuitBreakerRegistry: CircuitBreakerRegistry,
        meterRegistry: MeterRegistry,
    ): CircuitBreaker {
//        val cbProperties = properties.circuitbreaker ?: return createDefaultCircuitBreaker(circuitBreakerRegistry)
//
//        val config =
//            CircuitBreakerConfig
//                .custom()
//                .failureRateThreshold(cbProperties.failureRateThreshold)
//                .slowCallRateThreshold(cbProperties.slowCallRateThreshold)
//                .slowCallDurationThreshold(Duration.ofMillis(cbProperties.slowCallDurationThresholdMillis))
//                .waitDurationInOpenState(Duration.ofMillis(cbProperties.waitDurationInOpenStateMillis))
//                .permittedNumberOfCallsInHalfOpenState(cbProperties.permittedNumberOfCallsInHalfOpenState)
//                .automaticTransitionFromOpenToHalfOpenEnabled(
//                    cbProperties.automaticTransitionFromOpenToHalfOpenEnabled,
//                )
//                .recordExceptions(java.net.ConnectException::class.java)
//                .recordException(java.net.SocketTimeoutException::class.java)
//                .recordException(org.springframework.web.client.HttpServerErrorException::class.java)
//                .recordException(org.springframework.web.client.ResourceAccessException::class.java)
//                .build()

        val circuitBreaker =
            circuitBreakerRegistry.circuitBreaker(
                JSONPLACEHOLDER_CIRCUIT_BREAKER_NAME,
//                config,
            )

        // Register metrics
//        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry).bindTo(meterRegistry)

        // Register event listener
        circuitBreaker
            .eventPublisher
            .onStateTransition { event ->
                LOGGER.debug(
                    "Circuit Breaker '{}' transitioned from {} to {}",
                    event.circuitBreakerName,
                    event.stateTransition.fromState,
                    event.stateTransition.toState,
                )
            }

        return circuitBreaker
    }

    private fun createDefaultCircuitBreaker(circuitBreakerRegistry: CircuitBreakerRegistry): CircuitBreaker {
        val config =
            CircuitBreakerConfig
                .custom()
                .failureRateThreshold(DEFAULT_FAILURE_RATE_THRESHOLD)
                .slowCallRateThreshold(DEFAULT_SLOW_CALL_RATE_THRESHOLD)
                .slowCallDurationThreshold(Duration.ofSeconds(DEFAULT_SLOW_CALL_DURATION_THRESHOLD_SECONDS))
                .waitDurationInOpenState(Duration.ofSeconds(DEFAULT_WAIT_DURATION_IN_OPEN_STATE_SECONDS))
                .permittedNumberOfCallsInHalfOpenState(DEFAULT_PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build()

        return circuitBreakerRegistry.circuitBreaker(JSONPLACEHOLDER_CIRCUIT_BREAKER_NAME, config)
    }
}








