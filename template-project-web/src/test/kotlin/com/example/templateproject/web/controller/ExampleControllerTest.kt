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
        val exampleDTO1 = ExampleDTO(id1, "#$id1 example")
        val id2 = 20L
        val exampleDTO2 = ExampleDTO(id2, "#$id2 example")
        val pageSize = 10
        val pageRequest = PageRequest.ofSize(pageSize)

        val exampleDTOs = listOf(exampleDTO1, exampleDTO2)
        val sortOrders = listOf(SortOrder("createdDate", "DESC"), SortOrder("id", "DESC"))
        val pageDetails = PageDetails(exampleDTOs, 0, pageSize, exampleDTOs.size.toLong(), 1, true, sortOrders)
        every { exampleService.getExamples(pageRequest) } returns pageDetails

        // when
        val actual = victim.getExamples(pageRequest)

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
        val exampleDTO1 = ExampleDTO(id1, "#$id1 example")
        val id2 = 20L
        val exampleDTO2 = ExampleDTO(id2, "#$id2 example")
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
        val example = ExampleDTO(id, "#$id example")

        every { exampleService.getExample(id) } returns example

        // when
        val actual = victim.getExample(id)

        // then
        assertEquals(id, actual.id)
        assertEquals("#$id example", actual.name)
    }

    @Test
    fun `Should create example and redirect`() {
        // given
        val exampleDTO = ExampleDTO(name = "new example")

        every { exampleService.createExample(exampleDTO) } returns 33L

        // when
        val actual = victim.createExample(exampleDTO)

        // then
        assertEquals("/example/33", actual.url)
    }

    @Test
    fun `Should update example and redirect`() {
        // given
        val id = 10L
        val exampleDTO = ExampleDTO(id, "updated example")

        every { exampleService.updateExample(exampleDTO) } returns id

        // when
        val actual = victim.updateExample(exampleDTO)

        // then
        assertEquals("/example/10", actual.url)
    }

    @Test
    fun `Should delete example`() {
        // given
        val id = 10L

        every { exampleService.deleteExample(id) } returns Unit

        // when
        val actual = victim.deleteExample(id)

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
