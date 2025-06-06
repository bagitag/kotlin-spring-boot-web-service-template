package com.example.templateproject.client

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class GenericHttpClient {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(GenericHttpClient::class.java)
    }

    fun <RESPONSE> perform(
        logPrefix: String,
        request: Any,
        defaultResponse: RESPONSE,
        httpCall: () -> ResponseEntity<RESPONSE>,
    ): RESPONSE {
        LOGGER.debug("$logPrefix ==> Sending request: {}", request)

        var result = defaultResponse

        val responseEntity = httpCall()

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
