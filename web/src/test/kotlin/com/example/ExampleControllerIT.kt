package com.example

import com.example.dto.ExampleDTO
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExampleControllerIT(@Autowired val restTemplate: TestRestTemplate) {

	val mapper = ObjectMapper().registerKotlinModule()

	@Test
	fun testForGetAllExamples() {
		// when
		val actual = restTemplate.getForEntity("/example", String::class.java)

		// then
		assertEquals(HttpStatus.OK, actual.statusCode)
		val body = getResponseBody(actual.body, List::class.java)
		assertEquals(10, body.size)
		assertFalse(body.isNullOrEmpty())
	}

	@Test
	fun testForGetExample() {
		// given
		val id = 1

		// when
		val response = restTemplate.getForObject("/example/$id", String::class.java)
		val actual: ExampleDTO = mapper.readValue(response)

		// then
		assertEquals(id, actual.id)
		assertEquals("#$id example", actual.name)
	}

	@Test
	fun testForCreateExample() {
		// given
		val id = 10
		val name = "#$id example"
		val request = ExampleDTO(10, name)

		// when
		val actual = restTemplate.postForLocation("/example", request, String::class.java)

		// then
		assertTrue(actual.toString().endsWith("/example/$id"))
	}

	private fun <T> getResponseBody(body: String, clz: Class<T>): T {
		return mapper.readValue(body, clz)
	}
}
