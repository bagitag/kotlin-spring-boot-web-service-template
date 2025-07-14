package com.example.templateproject.client.jsonplaceholder

import org.springframework.retry.annotation.CircuitBreaker
import org.springframework.retry.annotation.Recover
import org.springframework.stereotype.Component

@Component
class JsonPlaceholderCircuitBreaker {
    @CircuitBreaker(
        label = "JSON_PLACEHOLDER_CB",
        maxAttemptsExpression = $$"#{'${client.jsonplaceholder.circuitbreaker.failure.rate}'}",
        openTimeoutExpression = $$"#{'${client.jsonplaceholder.circuitbreaker.open.timeout.millis:10000}'}",
        resetTimeoutExpression = $$"#{'${client.jsonplaceholder.circuitbreaker.reset.timeout.millis:10000}'}",
    )
    fun <T> decorate(method: () -> T) = method()

    @Recover
    private fun <T> recover(ex: Exception): T = throw ex
}
