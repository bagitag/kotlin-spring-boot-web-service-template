package com.example.templateproject.client.exception

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

@Component
class ExternalServiceExceptionHandler(
    private val jsonMapper: JsonMapper,
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ExternalServiceExceptionHandler::class.java)
    }

    fun getDetails(exception: ExternalServiceException): Map<String, Any> {
        val details = mutableMapOf<String, Any>("serviceName" to exception.serviceName)

        val responseBody =
            when (exception.cause) {
                is HttpClientErrorException, is HttpServerErrorException -> exception.cause.responseBodyAsString
                else -> null
            }

        if (!responseBody.isNullOrEmpty()) {
            details.putAll(processJsonResponse(responseBody, exception.serviceName))
        }

        return details
    }

    private fun processJsonResponse(
        response: String,
        serviceName: String,
    ): Map<String, Any> =
        try {
            val json = jsonMapper.readValue(response, Map::class.java) as Map<String, String>
            mapOf("response" to json)
        } catch (e: JacksonException) {
            LOGGER.warn("[{}] - Failed to parse server response: {}", serviceName, e.message)
            mapOf("rawResponse" to response)
        }
}
