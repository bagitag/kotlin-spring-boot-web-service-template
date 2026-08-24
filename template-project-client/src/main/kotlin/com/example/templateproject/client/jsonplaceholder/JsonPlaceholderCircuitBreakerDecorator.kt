package com.example.templateproject.client.jsonplaceholder

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.stereotype.Component

@Component
class JsonPlaceholderCircuitBreakerDecorator {

    @CircuitBreaker(name = "jsonplaceholder", fallbackMethod = "fallback")
    fun <T> decorate(method: () -> T) = method()

    fun <T> fallback(method: Function<T>, ex: Throwable) {
        print("AAAAAAAAAAAAA ex = ${ex.message}")
    }

}
