package com.example.templateproject.web.configuration

import com.example.templateproject.web.BaseIntegrationTest
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
class DebugHeaderFilterIT(
    @Autowired val restTemplate: TestRestTemplate,
) : BaseIntegrationTest() {
    @Test
    fun `Request with debug header should turn on debug level logging`(output: CapturedOutput) {
        // when
        val headers = HttpHeaders()
        headers.add(DebugHeaderFilter.DEBUG_REQUEST_HEADER_NAME, DebugHeaderFilter.DEBUG_REQUEST_HEADER_VALUE)
        val response =
            restTemplate.exchange(
                "$API_BASE_PATH/$EXAMPLE_ENDPOINT",
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                String::class.java,
            )

        // then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(output.out.contains("Debug level logging is turned on for ${DebugHeaderFilter.REQUEST_ID}:"))
    }
}
