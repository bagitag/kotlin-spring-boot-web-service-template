package com.example.templateproject.client

import com.example.templateproject.client.exception.ExternalServiceException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException

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
        LOGGER.debug("[$clientId] ==> Sending request: {}", request)

        var result = defaultResponse

        try {
            val responseEntity = httpCall()

            if (responseEntity.hasBody()) {
                result = responseEntity.body!!
                LOGGER.debug("[$clientId] <== Received response: {}", result)
            }

            if (!responseEntity.statusCode.is2xxSuccessful) {
                LOGGER.error("[$clientId] - Received unexpected response: {}", responseEntity)
            }
        } catch (e: Exception) {
            handleException(e, clientId)
        }
        return result
    }

    private fun handleException(e: Exception, clientId: String) {
        val message = when (e) {
            is HttpStatusCodeException -> "Communication error: ${e.statusText}"
            is ResourceAccessException -> e.message ?: "Resource access error"
            else -> "Unknown error"
        }
        throw ExternalServiceException(e, message, clientId)
    }
}
