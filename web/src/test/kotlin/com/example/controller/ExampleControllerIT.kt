package com.example.controller

import com.example.dto.ExampleDTO
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.servlet.view.RedirectView
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExampleControllerIT(@Autowired val restTemplate: TestRestTemplate) {

    val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testForGetAllExamples() {
        // when
        val actual = restTemplate.getForEntity("/example", String::class.java)

        // then
        assertEquals(HttpStatus.OK, actual.statusCode)
        val body = getResponseBody(actual.body!!, List::class.java)
        assertEquals(3, body.size)
        assertFalse(body.isNullOrEmpty())
    }

    @Test
    fun testForGetExample() {
        // given
        val id = 1L

        // when
        val actual = restTemplate.getForEntity(URI.create("/example/$id"), ExampleDTO::class.java)

        // then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(id, actual.body!!.id)
        assertEquals("#$id example", actual.body!!.name)
    }

    @Test
    fun testForCreateExample() {
        // given
        val name = "New example"
        val request = ExampleDTO(name = name)

        // when
        val actual = restTemplate.postForLocation("/example", request)

        // then
        assertTrue(actual.toString().endsWith("/example/4"))
    }

    @Test
    fun testForUpdateExample() {
        // given
        val id = 2L
        val name = "Updated example"
        val request = ExampleDTO(id, name)

        // when
        val actual =
            restTemplate.exchange(URI.create("/example"), HttpMethod.PUT, HttpEntity(request), RedirectView::class.java)

        // then
        assertEquals(HttpStatus.FOUND, actual.statusCode)
        val location = actual.headers.location!!.toString()
        assertTrue(location.endsWith("/example/$id"))
    }

    @Test
    fun testForDeleteExample() {
        // given
        val id = 3L

        // when
        val actual = restTemplate.exchange("/example/$id", HttpMethod.DELETE, null, ResponseEntity::class.java)

        // then
        assertEquals(HttpStatus.NO_CONTENT, actual.statusCode)
    }

    private fun <T> getResponseBody(body: String, clz: Class<T>): T {
        return mapper.readValue(body, clz)
    }
}
