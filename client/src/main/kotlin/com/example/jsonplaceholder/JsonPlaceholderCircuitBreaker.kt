package com.example.jsonplaceholder

import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.CircuitBreaker
import org.springframework.retry.annotation.Recover
import org.springframework.stereotype.Component

@Component
class JsonPlaceholderCircuitBreaker {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JsonPlaceholderCircuitBreaker::class.java)
    }

    @CircuitBreaker(
        label = "JSON_PLACEHOLDER_CB",
        maxAttemptsExpression = "#{'\${client.jsonplaceholder.circuitbreaker.failure.rate}'}",
        openTimeoutExpression = "#{'\${client.jsonplaceholder.circuitbreaker.open.timeout.millis:10000}'}",
        resetTimeoutExpression = "#{'\${client.jsonplaceholder.circuitbreaker.reset.timeout.millis:10000}'}"
    )
    fun <T> decorate(method : () -> T) = method()

    @Recover
    private fun <T> recover(ex: Exception): T {
        LOGGER.debug("[JSON_PLACEHOLDER] - JSON_PLACEHOLDER_CB circuit breaker error: ${ex.message}")
        throw ex
    }
}
