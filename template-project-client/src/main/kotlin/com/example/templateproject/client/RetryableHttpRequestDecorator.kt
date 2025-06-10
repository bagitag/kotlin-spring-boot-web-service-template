package com.example.templateproject.client

import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.annotation.Retryable
import org.springframework.retry.support.RetrySynchronizationManager
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException

@EnableRetry
@Component
class RetryableHttpRequestDecorator {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(RetryableHttpRequestDecorator::class.java)
    }

    @Retryable(
        label = "HTTP_SERVER_ERROR_RETRY",
        retryFor = [HttpServerErrorException::class, ResourceAccessException::class],
        maxAttemptsExpression = "#{'\${max.retry.attempts}'}",
        backoff = Backoff(delay = 500, multiplier = 2.0, maxDelay = 1000),
    )
    fun <T> retryForHttpServerError(
        request: Any,
        method: () -> T,
    ): T {
        LOGGER.debug("Retry count: {} for request: {}", RetrySynchronizationManager.getContext()!!.retryCount, request)
        return method()
    }
}
