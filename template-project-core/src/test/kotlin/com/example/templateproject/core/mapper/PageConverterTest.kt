package com.example.templateproject.core.mapper

import com.example.templateproject.core.util.anExampleDTO
import com.example.templateproject.persistence.repository.ExampleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

internal class PageConverterTest {

    private val victim = PageConverter()

    @Test
    fun `Should convert Spring's Page object to PageDetails`() {
        // given
        val exampleDTOs = listOf(anExampleDTO(1L), anExampleDTO(2L), anExampleDTO(3L), anExampleDTO(4L))
        val pageable = PageRequest.of(1, 2, ExampleRepository.DEFAULT_SORT)
        val page = PageImpl(exampleDTOs, pageable, exampleDTOs.size.toLong())

        // when
        val actual = victim.createPageDetails(page)

        // then
        assertEquals(exampleDTOs.size, actual.content.size)
        assertEquals(1, actual.pageNumber)
        assertEquals(2, actual.pageSize)
        assertEquals(exampleDTOs.size.toLong(), actual.totalElements)
        assertEquals(2, actual.totalPages)
        assertTrue(actual.sorted)
        val sortOrders = actual.sortOrders
        assertEquals(2, sortOrders.size)
        assertEquals("createdDate", sortOrders.first().property)
        assertEquals("DESC", sortOrders.first().direction)
        assertEquals("id", sortOrders.last().property)
        assertEquals("DESC", sortOrders.last().direction)
    }
}
