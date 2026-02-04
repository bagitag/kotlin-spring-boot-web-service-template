package com.example.templateproject.client

import com.example.templateproject.client.exception.ExternalServiceException
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.retry.RetryListener
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.core.retry.Retryable
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.time.Duration

@Configuration
class RetryTemplateConfiguration(
    private val retryProperties: RetryProperties,
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(RetryTemplateConfiguration::class.java)
        private const val RETRY_JITTER_MILLIS = 100L
    }

    @Bean
    fun retryTemplateForHttpServerError(): RetryTemplate {
        val retryPolicy =
            RetryPolicy
                .builder()
                .includes(HttpServerErrorException::class.java, ResourceAccessException::class.java)
                .maxRetries(retryProperties.maximumRetries)
                .delay(Duration.ofMillis(retryProperties.delay.millis))
                .maxDelay(Duration.ofMillis(retryProperties.delay.maximumMillis))
                .multiplier(retryProperties.delay.multiplier)
                .jitter(Duration.ofMillis(RETRY_JITTER_MILLIS))
                .timeout(Duration.ofSeconds(retryProperties.timeout))
                .build()

        return RetryTemplate(retryPolicy).also {
            it.retryListener = retryListener()
        }
    }

    private fun retryListener() =
        object : RetryListener {
            override fun onRetryFailure(
                retryPolicy: RetryPolicy,
                retryable: Retryable<*>,
                throwable: Throwable,
            ) {
                super.onRetryFailure(retryPolicy, retryable, throwable)

                val serviceName =
                    if (throwable is ExternalServiceException) {
                        "[${throwable.serviceName}] "
                    } else {
                        ""
                    }
                LOGGER.debug("{}Retry failed: {}", serviceName, throwable.message)
            }
        }
}
