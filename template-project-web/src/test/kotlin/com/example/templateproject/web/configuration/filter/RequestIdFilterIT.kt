package com.example.templateproject.web.configuration.filter

import com.example.templateproject.web.BaseIntegrationTest
import com.example.templateproject.web.configuration.API_BASE_PATH
import com.example.templateproject.web.controller.EXAMPLE_ENDPOINT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@ExtendWith(OutputCaptureExtension::class)
class RequestIdFilterIT(@Autowired val restTemplate: TestRestTemplate) : BaseIntegrationTest() {

    @Test
    fun `Should use request id form header`(output: CapturedOutput) {
        // when
        val headers = HttpHeaders()
        headers.add(RequestIdFilter.REQUEST_ID_HEADER, "test-request-id")
        val response =
            restTemplate.exchange(
                "${API_BASE_PATH}/$EXAMPLE_ENDPOINT",
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                String::class.java
            )

        // then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(output.out.contains("[requestId=test-request-id"))
    }

    @Test
    fun `Should generate request id`(output: CapturedOutput) {
        // when
        val headers = HttpHeaders()
        val response =
            restTemplate.exchange(
                "${API_BASE_PATH}/$EXAMPLE_ENDPOINT",
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                String::class.java
            )

        // then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(
            output.out.contains(".*requestId=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex()))
        assertTrue(output.out.contains("[requestId="))
    }
}
