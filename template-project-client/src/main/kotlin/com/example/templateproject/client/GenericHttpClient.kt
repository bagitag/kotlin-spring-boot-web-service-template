package com.example.templateproject.client

import com.example.templateproject.client.exception.ExternalServiceException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException

@Component
class GenericHttpClient {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(GenericHttpClient::class.java)
    }

    fun <RESPONSE> perform(
        clientId: String,
        request: Any,
        defaultResponse: RESPONSE,
        httpCall: () -> ResponseEntity<RESPONSE>,
    ): RESPONSE {
        LOGGER.debug("[{}] ==> Sending request: {}", clientId, request)

        var result = defaultResponse

        try {
            val responseEntity = httpCall()

            if (responseEntity.hasBody()) {
                result = responseEntity.body!!
                LOGGER.debug("[{}] <== Received response: {}", clientId, result)
            }

            if (!responseEntity.statusCode.is2xxSuccessful) {
                LOGGER.error("[{}] - Received unexpected response: {}", clientId, responseEntity)
            }
        } catch (e: Exception) {
            handleException(e, clientId)
        }
        return result
    }

    private fun handleException(
        e: Exception,
        clientId: String,
    ) {
        val message =
            when (e) {
                is HttpStatusCodeException -> "Communication error: ${e.statusText}"
                is ResourceAccessException -> e.message ?: "Resource access error"
                is RestClientException -> e.message ?: "REST client error"
                else -> "Unknown error"
            }
        throw ExternalServiceException(e, message, clientId)
    }
}
