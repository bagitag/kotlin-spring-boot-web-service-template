package com.example.templateproject.client.exception

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

@ExtendWith(MockKExtension::class)
class ExternalServiceExceptionHandlerTest {
    @MockK
    private lateinit var jsonMapper: JsonMapper

    @InjectMockKs
    private lateinit var victim: ExternalServiceExceptionHandler

    @Test
    fun `Should return service name as details`() {
        // given
        val serviceName = "testService"
        val exception = ResourceAccessException("Test exception")
        val externalServiceException = ExternalServiceException(exception, "Test message", serviceName)

        // when
        val actual = victim.getDetails(externalServiceException)

        // then
        assert(actual["serviceName"] == serviceName) { "Service name should be included in details" }
    }

    @Test
    fun `Should not return response when body is null`() {
        // given
        val serviceName = "testService"
        val exception = HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null)
        val externalServiceException = ExternalServiceException(exception, "Test message", serviceName)

        // when
        val actual = victim.getDetails(externalServiceException)

        // then
        assert(actual["serviceName"] == serviceName) { "Service name should be included in details" }
        assert(actual["response"] == null) { "Raw response should be null when response body is null" }
        assert(actual["rawResponse"] == null) { "Raw response should be null when response body is null" }
    }

    @Test
    fun `Should return raw response when parsing fails`() {
        // given
        val serviceName = "testService"
        val response = "NOT A JSON RESPONSE"
        val exception =
            HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", null, response.toByteArray(), null)
        val externalServiceException = ExternalServiceException(exception, "Test message", serviceName)

        val jacksonException = JacksonException.wrapWithPath(Exception("Parsing error"), Any(), "response")
        every { jsonMapper.readValue(response, Map::class.java) } throws jacksonException

        // when
        val actual = victim.getDetails(externalServiceException)

        // then
        assert(actual["serviceName"] == serviceName) { "Service name should be included in details" }
        assert(actual["rawResponse"] == response) { "Raw response should be included in details" }
        assertNull(actual["response"]) { "Response should be null when parsing fails" }
    }

    @Test
    fun `Should return parsed response`() {
        // given
        val serviceName = "testService"
        val response = """{"key1": "value1", "key2": "value2"}"""
        val parsedResponse = mapOf("key1" to "value1", "key2" to "value2")
        val exception =
            HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                null,
                response.toByteArray(),
                null,
            )
        val externalServiceException = ExternalServiceException(exception, "Test message", serviceName)

        every { jsonMapper.readValue(response, Map::class.java) } returns parsedResponse

        // when
        val actual = victim.getDetails(externalServiceException)

        // then
        assert(actual["serviceName"] == serviceName) { "Service name should be included in details" }
        assert(actual["response"] == parsedResponse) { "Parsed response should be included in details" }
    }
}
