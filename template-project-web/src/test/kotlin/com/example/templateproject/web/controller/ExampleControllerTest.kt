package com.example.templateproject.web.controller

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.api.dto.SortOrder
import com.example.templateproject.core.service.ExampleService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus

@ExtendWith(MockKExtension::class)
internal class ExampleControllerTest {

    @MockK
    private lateinit var exampleService: ExampleService
    @InjectMockKs
    private lateinit var victim: ExampleController

    @Test
    fun `Should return paginated examples`() {
        // given
        val id1 = 10L
        val exampleDTO1 = ExampleDTO("#$id1 example").apply { id = id1 }
        val id2 = 20L
        val exampleDTO2 = ExampleDTO("#$id2 example").apply { id = id2 }
        val pageSize = 10
        val pageRequest = PageRequest.ofSize(pageSize)

        val exampleDTOs = listOf(exampleDTO1, exampleDTO2)
        val sortOrders = listOf(SortOrder("createdDate", "DESC"), SortOrder("id", "DESC"))
        val pageDetails = PageDetails(exampleDTOs, 0, pageSize, exampleDTOs.size.toLong(), 1, true, sortOrders)
        every { exampleService.getEntities(pageRequest) } returns pageDetails

        // when
        val actual = victim.getEntities(pageRequest)

        // then
        assertEquals(2, actual.content.size)
        assertEquals(0, actual.pageNumber)
        assertEquals(pageSize, actual.pageSize)
        assertEquals(exampleDTOs.size.toLong(), actual.totalElements)
        assertEquals(1, actual.totalPages)
        assertTrue(actual.sorted)
        assertEquals(sortOrders.size, actual.sortOrders.size)
    }

    @Test
    fun `Should return paginated examples based on given search term`() {
        // given
        val id1 = 10L
        val exampleDTO1 = ExampleDTO("#$id1 example").apply { id = id1 }
        val id2 = 20L
        val exampleDTO2 = ExampleDTO("#$id2 example").apply { id = id2 }
        val pageSize = 10
        val pageRequest = PageRequest.ofSize(10)
        val searchTerm = listOf("exa")

        val exampleDTOs = listOf(exampleDTO1, exampleDTO2)
        val sortOrders = listOf(SortOrder("createdDate", "DESC"), SortOrder("id", "DESC"))
        val pageDetails = PageDetails(exampleDTOs, 0, pageSize, exampleDTOs.size.toLong(), 1, true, sortOrders)
        every { exampleService.searchExamples(searchTerm, pageRequest) } returns pageDetails

        // when
        val actual = victim.searchExamples(pageRequest, searchTerm)

        // then
        assertEquals(2, actual.content.size)
        assertEquals(0, actual.pageNumber)
        assertEquals(pageSize, actual.pageSize)
        assertEquals(exampleDTOs.size.toLong(), actual.totalElements)
        assertEquals(1, actual.totalPages)
        assertTrue(actual.sorted)
        assertEquals(sortOrders.size, actual.sortOrders.size)
    }

    @Test
    fun `Should return example by given id`() {
        // given
        val id = 10L
        val example = ExampleDTO("#$id example").apply { this.id = id }

        every { exampleService.getEntityById(id) } returns example

        // when
        val actual = victim.getEntityById(id)

        // then
        assertEquals(id, actual.id)
        assertEquals("#$id example", actual.name)
    }

    @Test
    fun `Should create example`() {
        // given
        val id = 33L
        val requestExampleDTO = ExampleDTO(name = "new example")
        val responseExampleDTO = ExampleDTO("new example").apply { this.id = id }

        every { exampleService.createEntity(requestExampleDTO) } returns responseExampleDTO

        // when
        val actual = victim.createEntity(requestExampleDTO)

        // then
        assertEquals(responseExampleDTO, actual.body)
        assertEquals(id, actual.body!!.id)
    }

    @Test
    fun `Should update example`() {
        // given
        val id = 10L
        val exampleDTO = ExampleDTO("updated example").apply { this.id = id }

        every { exampleService.updateEntity(exampleDTO) } returns exampleDTO

        // when
        val actual = victim.updateEntity(exampleDTO)

        // then
        assertEquals(exampleDTO, actual)
    }

    @Test
    fun `Should delete example`() {
        // given
        val id = 10L

        every { exampleService.deleteEntity(id) } returns Unit

        // when
        val actual = victim.deleteEntity(id)

        // then
        assertEquals(HttpStatus.NO_CONTENT, actual.statusCode)
    }

    @Test
    fun `Should return word statistics`() {
        // given
        val result = mapOf("user1" to 123, "user2" to 323, "user3" to 545)

        every { exampleService.getWordCountForUsers() } returns result

        // when
        val actual = victim.getWordStatistics()

        // then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertNotNull(actual.body)
        assertEquals(result.size, actual.body!!.size)
    }
}
