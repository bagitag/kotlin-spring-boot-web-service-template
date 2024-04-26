package com.example.controller

import com.example.dto.ExampleDTO
import com.example.dto.PageDetails
import com.example.dto.SortOrder
import com.example.service.ExampleService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus

@ExtendWith(MockKExtension::class)
internal class ExampleControllerTest {

    @MockK
    private lateinit var exampleService: ExampleService

    private lateinit var victim: ExampleController

    @BeforeEach
    fun initialize() {
        victim = ExampleController(exampleService)
    }

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
        val pageDetails = PageDetails(exampleDTOs, 0, pageSize, exampleDTOs.size.toLong(), 1,
            true, setOf(SortOrder("createdDate", "DESC")))
        every { exampleService.getExamples(pageRequest) } returns pageDetails

        // when
        val actual = victim.getExamples(pageRequest)

        // then
        Assertions.assertEquals(2, actual.content.size)
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
        val pageDetails = PageDetails(exampleDTOs, 0, pageSize, exampleDTOs.size.toLong(), 1,
            true, setOf(SortOrder("createdDate", "DESC")))
        every { exampleService.searchExamples(searchTerm, pageRequest) } returns pageDetails

        // when
        val actual = victim.searchExamples(pageRequest, searchTerm)

        // then
        Assertions.assertEquals(2, actual.content.size)
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
        Assertions.assertEquals(id, actual.id)
        Assertions.assertEquals("#$id example", actual.name)
    }

    @Test
    fun `Should create example and redirect`() {
        // given
        val exampleDTO = ExampleDTO(name = "new example")

        every { exampleService.createExample(exampleDTO) } returns 33L

        // when
        val actual = victim.createExample(exampleDTO)

        // then
        Assertions.assertEquals("/example/33", actual.url)
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
        Assertions.assertEquals("/example/10", actual.url)
    }

    @Test
    fun `Should delete example`() {
        // given
        val id = 10L

        every { exampleService.deleteExample(id) } returns Unit

        // when
        val actual = victim.deleteExample(id)

        // then
        Assertions.assertEquals(HttpStatus.NO_CONTENT, actual.statusCode)
    }
}
