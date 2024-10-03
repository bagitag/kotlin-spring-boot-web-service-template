package com.example.jsonplaceholder

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import java.util.function.Supplier

@Component
class RetryableHttpClient {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RetryableHttpClient::class.java)
    }

    @Retryable(
        retryFor = [ HttpServerErrorException::class ],
        maxAttempts = 3,
        backoff = Backoff(delay = 500)
    )
    fun <RESPONSE> retryForHttpServerError(
        logPrefix: String,
        request: Any,
        defaultResponse: RESPONSE,
        supplier: Supplier<ResponseEntity<RESPONSE>>
    ): RESPONSE {
        LOGGER.debug("$logPrefix ==> Sending request: {}", request)

        var result = defaultResponse

        val responseEntity = supplier.get()

        if (responseEntity.hasBody()) {
            result = responseEntity.body!!
            LOGGER.debug("$logPrefix <== Received response: {}", result)
        }

        if (!responseEntity.statusCode.is2xxSuccessful) {
            LOGGER.error("$logPrefix - Received unexpected response: {}", responseEntity)
        }
        return result
    }
}
