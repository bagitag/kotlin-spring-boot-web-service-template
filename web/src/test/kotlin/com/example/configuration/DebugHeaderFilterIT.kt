package com.example.configuration

import com.example.BaseIntegrationTest
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
class DebugHeaderFilterIT(@Autowired val restTemplate: TestRestTemplate): BaseIntegrationTest() {

    @Test
    fun `Request with debug header should turn on debug level logging`(output: CapturedOutput) {
        // when
        val headers = HttpHeaders()
        headers.add(DebugHeaderFilter.DEBUG_REQUEST_HEADER_NAME, DebugHeaderFilter.DEBUG_REQUEST_HEADER_VALUE)
        val response = restTemplate.exchange("/example", HttpMethod.GET, HttpEntity<Any>(headers), String::class.java)

        // then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(output.out.contains("Debug level logging is turned on for ${DebugHeaderFilter.REQUEST_ID}:"))
    }
}
