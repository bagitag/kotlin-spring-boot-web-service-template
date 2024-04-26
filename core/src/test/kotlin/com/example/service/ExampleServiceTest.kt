package com.example.service

import com.example.dto.ExampleDTO
import com.example.dto.PageDetails
import com.example.dto.SortOrder
import com.example.entity.Example
import com.example.exception.IdNotFoundException
import com.example.mapper.ExampleMapper
import com.example.mapper.PageConverter
import com.example.repository.ExampleRepository
import com.example.util.anExample
import com.example.util.anExampleDTOFromEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ExampleServiceTest {

    private val exampleRepository = mockk<ExampleRepository>()
    @MockK
    private lateinit var exampleMapper: ExampleMapper
    @MockK
    private lateinit var pageConverter: PageConverter
    @InjectMockKs
    private lateinit var victim: ExampleService

    @Test
    fun `Should return paginated examples with default sort`() {
        // given
        val id1 = 100L
        val id2 = 200L
        val example1 = anExample(id1)
        val example2 = anExample(id2)
        val examples = PageImpl(listOf(example1, example2))
        val pageRequest = PageRequest.of(0, 10)
        val pageable = PageRequest.of(0, 10, ExampleRepository.DEFAULT_SORT)

        every { exampleRepository.findAll(pageable) } returns examples
        val exampleDTO1 = anExampleDTOFromEntity(example1)
        every { exampleMapper.toDTO(example1) } returns exampleDTO1
        val exampleDTO2 = anExampleDTOFromEntity(example2)
        every { exampleMapper.toDTO(example2) } returns exampleDTO2

        val exampleDTOs = listOf(exampleDTO1, exampleDTO2)
        val pageDetails = PageDetails(exampleDTOs, 0, 10, 2, 1, true, setOf(SortOrder("createdDate", "DESC")))
        every { pageConverter.createPageDetails(any(PageImpl::class)) } returns pageDetails

        // when
        val actual = victim.getExamples(pageRequest)

        // then
        verifySequence {
            exampleRepository.findAll(pageable)
            exampleMapper.toDTO(example1)
            exampleMapper.toDTO(example2)
        }
        assertEquals(examples.size, actual.content.size)
        assertEquals(10, actual.pageSize)
        assertEquals(0, actual.pageNumber)
        assertTrue(actual.sorted)
        assertEquals(1, actual.sortOrders.size)
        assertEquals("createdDate", actual.sortOrders.first().property)
        assertEquals("DESC", actual.sortOrders.first().direction)
        assertEquals(id2, actual.content.last().id)
        assertEquals("$id2. example", actual.content.last().name)
    }

    @Test
    fun `Should return paginated examples with custom sort`() {
        // given
        val id1 = 100L
        val id2 = 200L
        val id3 = 300L
        val example1 = anExample(id1)
        val example2 = anExample(id2)
        val example3 = anExample(id3)
        val examples = PageImpl(listOf(example3, example2, example1))
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"id"))

        every { exampleRepository.findAll(pageable) } returns examples
        val exampleDTO3 = anExampleDTOFromEntity(example3)
        every { exampleMapper.toDTO(example3) } returns exampleDTO3
        val exampleDTO2 = anExampleDTOFromEntity(example2)
        every { exampleMapper.toDTO(example2) } returns exampleDTO2
        val exampleDTO1 = anExampleDTOFromEntity(example1)
        every { exampleMapper.toDTO(example1) } returns exampleDTO1

        val exampleDTOs = listOf(exampleDTO3, exampleDTO2, exampleDTO1)
        val pageDetails = PageDetails(exampleDTOs, 0, 10, 2, 1, true, setOf(SortOrder("id", "DESC")))
        every { pageConverter.createPageDetails(any(PageImpl::class)) } returns pageDetails

        // when
        val actual = victim.getExamples(pageable)

        // then
        verifySequence {
            exampleRepository.findAll(pageable)
            exampleMapper.toDTO(example3)
            exampleMapper.toDTO(example2)
            exampleMapper.toDTO(example1)
        }
        assertEquals(examples.size, actual.content.size)
        assertEquals(10, actual.pageSize)
        assertEquals(0, actual.pageNumber)
        assertTrue(actual.sorted)
        assertEquals(1, actual.sortOrders.size)
        assertEquals("id", actual.sortOrders.first().property)
        assertEquals("DESC", actual.sortOrders.first().direction)
        assertEquals(id1, actual.content.last().id)
        assertEquals("$id1. example", actual.content.last().name)
    }

    @Test
    fun `Should return examples containing search term`() {
        // given
        val id1 = 199L
        val id2 = 399L
        val example1 = anExample(id1)
        val example2 = anExample(id2)
        val examples = PageImpl(listOf(example2, example1))
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC,"id"))
        val searchTerm = listOf("99")

        every { exampleRepository.findByNameInIgnoreCase(searchTerm, pageable) } returns examples
        val exampleDTO2 = anExampleDTOFromEntity(example2)
        every { exampleMapper.toDTO(example2) } returns exampleDTO2
        val exampleDTO1 = anExampleDTOFromEntity(example1)
        every { exampleMapper.toDTO(example1) } returns exampleDTO1

        val exampleDTOs = listOf(exampleDTO1, exampleDTO2)
        val pageDetails = PageDetails(exampleDTOs, 0, 10, 2, 1, true, setOf(SortOrder("id", "DESC")))
        every { pageConverter.createPageDetails(any(PageImpl::class)) } returns pageDetails

        // when
        val actual = victim.searchExamples(searchTerm, pageable)

        // then
        verifySequence {
            exampleRepository.findByNameInIgnoreCase(searchTerm, pageable)
            exampleMapper.toDTO(example2)
            exampleMapper.toDTO(example1)
        }
        assertEquals(examples.size, actual.content.size)
        assertEquals(10, actual.pageSize)
        assertEquals(0, actual.pageNumber)
        assertTrue(actual.sorted)
        assertEquals(1, actual.sortOrders.size)
        assertEquals("id", actual.sortOrders.first().property)
        assertEquals("DESC", actual.sortOrders.first().direction)
        assertEquals(id1, actual.content.first().id)
        assertEquals("$id1. example", actual.content.first().name)
    }

    @Test
    fun `Should return example for the given id`() {
        // given
        val id = 8L
        val example = anExample(id)

        every { exampleRepository.findById(id) } returns Optional.of(example)
        every { exampleMapper.toDTO(example) } returns anExampleDTOFromEntity(example)

        // when
        val actual = victim.getExample(id)

        // then
        verifySequence {
            exampleRepository.findById(id)
            exampleMapper.toDTO(example)
        }
        assertEquals(id, actual.id)
        assertEquals("$id. example", actual.name)
    }

    @Test
    fun `Should return exception for the given id`() {
        // given
        val id = 100L
        every { exampleRepository.findById(id) } returns Optional.empty()

        // when
        assertThrows<IdNotFoundException> { victim.getExample(id) }

        // then
        verifySequence {
            exampleRepository.findById(id)
        }
    }

    @Test
    fun `Should create new example and return id`() {
        // given
        val id = 99L
        val name = "test example"
        val exampleDTO = ExampleDTO(id, name)
        val example = Example(id, name)

        every { exampleMapper.fromDTO(exampleDTO) } returns example
        every { exampleRepository.save(example) } returns example

        // when
        val actual = victim.createExample(exampleDTO)

        // then
        verifySequence {
            exampleMapper.fromDTO(exampleDTO)
            exampleRepository.save(example)
        }
        assertEquals(example.id, actual)
    }

    @Test
    fun `Should update existing example and return id`() {
        // given
        val id = 99L
        val name = "test example"
        val exampleDTO = ExampleDTO(id, name)
        val example = Example(id, name)

        every { exampleRepository.findById(id) } returns Optional.of(example)
        every { exampleMapper.fromDTO(exampleDTO) } returns example
        every { exampleRepository.save(example) } returns example

        // when
        val actual = victim.updateExample(exampleDTO)

        // then
        verifySequence {
            exampleRepository.findById(id)
            exampleMapper.fromDTO(exampleDTO)
            exampleRepository.save(example)
        }
        assertEquals(example.id, actual)
    }

    @Test
    fun `Update should return exception if the id is missing`() {
        // given
        val name = "test example"
        val exampleDTO = ExampleDTO(name = name)

        // when
        assertThrows<NullPointerException> { victim.updateExample(exampleDTO) }
    }

    @Test
    fun `Update should return IdNotFoundException`() {
        // given
        val id = 100L
        val name = "test example"
        val exampleDTO = ExampleDTO(id, name)

        every { exampleRepository.findById(id) } returns Optional.empty()

        // when
        assertThrows<IdNotFoundException> { victim.updateExample(exampleDTO) }

        // then
        verify {
            exampleRepository.findById(id)
        }
    }

    @Test
    fun `Should delete example`() {
        // given
        val id = 99L

        every { exampleRepository.deleteById(id) } returns mockk()

        // when
        victim.deleteExample(id)

        // then
        verifySequence {
            exampleRepository.deleteById(id)
        }
    }
}
