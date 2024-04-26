package com.example.controller

import com.example.BaseIntegrationTest
import com.example.dto.ExampleDTO
import com.example.dto.PageDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class ExampleControllerIT(@Autowired val restTemplate: TestRestTemplate): BaseIntegrationTest() {

    @Test
    fun testForGetPaginatedExamples() {
        // when
        val pageDetailsTypeReference = object : ParameterizedTypeReference<PageDetails<ExampleDTO>>() {}
        val actual = restTemplate.exchange(EXAMPLE_ENDPOINT, HttpMethod.GET, null, pageDetailsTypeReference)

        // then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(0, actual.body!!.pageNumber)
        assertEquals(10, actual.body!!.pageSize)
        assertEquals(15, actual.body!!.totalElements)
        assertEquals(2, actual.body!!.totalPages)
        assertTrue(actual.body!!.sorted)
        assertEquals(1, actual.body!!.sortOrders.size)
        assertEquals("createdDate", actual.body!!.sortOrders.first().property)
        assertEquals("DESC", actual.body!!.sortOrders.first().direction)
    }

    @Test
    fun testForGetAllPaginatedAndSortedExamples() {
        // when
        val uriBuilder = UriComponentsBuilder.fromUri(URI.create(EXAMPLE_ENDPOINT))
            .queryParam("page", 0)
            .queryParam("size", 50)
            .queryParam("sort", "id,asc", "name,desc")
        val pageTypeReference = object : ParameterizedTypeReference<PageDetails<ExampleDTO>>() {}
        val actual = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, null, pageTypeReference)

        // then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(0, actual.body!!.pageNumber)
        assertEquals(50, actual.body!!.pageSize)
        assertEquals(16, actual.body!!.totalElements)
        assertEquals(1, actual.body!!.totalPages)
        assertTrue(actual.body!!.sorted)
        assertEquals(2, actual.body!!.sortOrders.size)
        assertEquals("id", actual.body!!.sortOrders.first().property)
        assertEquals("ASC", actual.body!!.sortOrders.first().direction)
        assertEquals("name", actual.body!!.sortOrders.last().property)
        assertEquals("DESC", actual.body!!.sortOrders.last().direction)
    }

    @Test
    fun testForSearchExamples() {
        // when
        val searchTerms1 = "4. example"
        val searchTerms2 = "14. example"
        val uriBuilder = UriComponentsBuilder.fromUri(URI.create("$EXAMPLE_ENDPOINT/search"))
            .queryParam("searchTerms", searchTerms1, searchTerms2)
            .build(false)
        val pageTypeReference = object : ParameterizedTypeReference<PageDetails<ExampleDTO>>() {}
        val actual = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, null, pageTypeReference)

        // then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(0, actual.body!!.pageNumber)
        assertEquals(10, actual.body!!.pageSize)
        assertEquals(2, actual.body!!.totalElements)
        assertEquals(1, actual.body!!.totalPages)
        assertTrue(actual.body!!.sorted)
        assertEquals(searchTerms2, actual.body!!.content.first().name)
        assertEquals(searchTerms1, actual.body!!.content.last().name)
    }

    @Test
    fun testForGetExample() {
        // given
        val id = 1L

        // when
        val actual = restTemplate.getForEntity(URI.create("$EXAMPLE_ENDPOINT/$id"), ExampleDTO::class.java)

        // then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(id, actual.body!!.id)
        assertEquals("$id. example", actual.body!!.name)
    }

    @Test
    fun testForCreateExample() {
        // given
        val name = "New example"
        val request = ExampleDTO(name = name)

        // when
        val actual = restTemplate.postForLocation(EXAMPLE_ENDPOINT, request)

        // then
        assertTrue(actual.toString().endsWith("$EXAMPLE_ENDPOINT/16"))
    }

    @Test
    fun testForUpdateExample() {
        // given
        val id = 2L
        val name = "Updated example"
        val request = ExampleDTO(id, name)

        // when
        val actual = restTemplate.exchange(URI.create(EXAMPLE_ENDPOINT), HttpMethod.PUT, HttpEntity(request),
            RedirectView::class.java)

        // then
        assertEquals(HttpStatus.FOUND, actual.statusCode)
        val location = actual.headers.location!!.toString()
        assertTrue(location.endsWith("$EXAMPLE_ENDPOINT/$id"))
    }

    @Test
    fun testForDeleteExample() {
        // given
        val id = 3L

        // when
        val actual = restTemplate.exchange("$EXAMPLE_ENDPOINT/$id", HttpMethod.DELETE, null, ResponseEntity::class.java)

        // then
        assertEquals(HttpStatus.NO_CONTENT, actual.statusCode)
    }
}
