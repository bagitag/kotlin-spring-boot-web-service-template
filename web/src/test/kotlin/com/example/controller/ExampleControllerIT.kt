package com.example.controller

import com.example.BaseIntegrationTest
import com.example.dto.ExampleDTO
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.servlet.view.RedirectView
import java.net.URI

class ExampleControllerIT(@Autowired val restTemplate: TestRestTemplate): BaseIntegrationTest() {

    val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testForGetAllExamples() {
        // when
        val actual = restTemplate.getForEntity("/example", String::class.java)

        // then
        assertEquals(HttpStatus.OK, actual.statusCode)
        val body = getResponseBody(actual.body!!, List::class.java)
        assertEquals(3, body.size)
        assertFalse(body.isEmpty())
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

    @Nested
    inner class DatabaseManipulatingTests {

        @Inject
        private lateinit var jdbcTemplate: JdbcTemplate

        @BeforeEach
        fun init() {
            jdbcTemplate.execute("TRUNCATE TABLE example")
            jdbcTemplate.execute("ALTER SEQUENCE example_id_seq RESTART")

            try {
                val sqlScriptResource = ClassPathResource("data.sql")
                val sqlScript = sqlScriptResource.inputStream.bufferedReader().use { it.readText() }
                jdbcTemplate.execute(sqlScript)
            } catch (ex: Exception) {
                println("Error executing SQL script: ${ex.message}")
            }
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
            val actual = restTemplate.exchange(URI.create("/example"), HttpMethod.PUT, HttpEntity(request),
                RedirectView::class.java)

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
    }

    private fun <T> getResponseBody(body: String, clz: Class<T>): T {
        return mapper.readValue(body, clz)
    }
}
