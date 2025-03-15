package com.example.templateproject.web.controller

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.persistence.entity.history.ExampleHistory
import com.example.templateproject.persistence.entity.history.HistoryEvent
import com.example.templateproject.persistence.repository.history.ExampleHistoryRepository
import com.example.templateproject.web.BaseIntegrationTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
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
        assertEquals(2, actual.body!!.sortOrders.size)
        assertEquals("createdDate", actual.body!!.sortOrders.first().property)
        assertEquals("DESC", actual.body!!.sortOrders.first().direction)
        assertEquals("id", actual.body!!.sortOrders.last().property)
        assertEquals("DESC", actual.body!!.sortOrders.last().direction)
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
        assertEquals(15, actual.body!!.totalElements)
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

    @Nested
    inner class DatabaseManipulatingTests {

        @Inject
        private lateinit var historyRepository: ExampleHistoryRepository
        @Inject
        private lateinit var jdbcTemplate: JdbcTemplate

        @BeforeEach
        fun init() {
            jdbcTemplate.execute("TRUNCATE TABLE example")
            jdbcTemplate.execute("ALTER SEQUENCE example_id_seq RESTART")
            jdbcTemplate.execute("TRUNCATE TABLE example_history")
            jdbcTemplate.execute("ALTER SEQUENCE example_history_id_seq RESTART")

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
            val name = "1. new example"
            val request = ExampleDTO(name = name)

            // when
            val actual =
                restTemplate.postForEntity<ExampleDTO>(URI.create(EXAMPLE_ENDPOINT), request, ExampleDTO::class.java)

            // then
            assertEquals(HttpStatus.CREATED, actual.statusCode)
            val entity = actual.body!!
            assertEquals(name, entity.name)
            assertEquals(16, entity.id)

            val historyEntityList = historyRepository.findByEntityId(entity.id!!)
            assertEquals(1, historyEntityList.size)
            val historyEntity = historyEntityList.first()
            validateHistoryEntity(entity, historyEntity, HistoryEvent.CREATE)
        }

        @Test
        fun testForUpdateExample() {
            // given
            val id = 2L
            val name = "$id updated example"
            val request = ExampleDTO(name).apply { this.id = id }

            // when
            val actual = restTemplate.exchange(
                URI.create(EXAMPLE_ENDPOINT), HttpMethod.PUT, HttpEntity(request),
                ExampleDTO::class.java
            )

            // then
            assertEquals(HttpStatus.OK, actual.statusCode)
            val entity = actual.body!!
            assertEquals(id, entity.id)
            assertEquals(name, entity.name)

            val historyEntityList = historyRepository.findByEntityIdAndEvent(entity.id!!, HistoryEvent.UPDATE)
            assertEquals(1, historyEntityList.size)
            val historyEntity = historyEntityList.first()
            validateHistoryEntity(entity, historyEntity, HistoryEvent.UPDATE)
        }

        @Test
        fun testForDeleteExample() {
            // given
            val id = 3L

            // when
            val actual =
                restTemplate.exchange("$EXAMPLE_ENDPOINT/$id", HttpMethod.DELETE, null, ResponseEntity::class.java)

            // then
            assertEquals(HttpStatus.NO_CONTENT, actual.statusCode)

            val historyEntityList = historyRepository.findByEntityIdAndEvent(id, HistoryEvent.DELETE)
            assertEquals(1, historyEntityList.size)
            val historyEntity = historyEntityList.first()
            assertEquals(HistoryEvent.DELETE, historyEntity.event)
        }

        private fun validateHistoryEntity(entity: ExampleDTO, historyEntity: ExampleHistory, event: HistoryEvent) {
            assertEquals(16, historyEntity.id)
            assertEquals(entity.id, historyEntity.entityId)
            assertEquals(entity.name, historyEntity.name)
            assertEquals(event, historyEntity.event)
            assertNotNull(historyEntity.createdAt)
        }
    }
}
