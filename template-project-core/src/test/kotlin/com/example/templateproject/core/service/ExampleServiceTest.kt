package com.example.templateproject.core.service

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.api.dto.SortOrder
import com.example.templateproject.client.jsonplaceholder.JsonPlaceholderService
import com.example.templateproject.client.jsonplaceholder.api.Post
import com.example.templateproject.client.jsonplaceholder.api.User
import com.example.templateproject.core.exception.IdNotFoundException
import com.example.templateproject.core.mapper.ExampleMapper
import com.example.templateproject.core.mapper.PageConverter
import com.example.templateproject.core.util.anExample
import com.example.templateproject.core.util.anExampleDTOFromEntity
import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.persistence.repository.ExampleRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.Optional
import java.util.concurrent.CompletableFuture

@ExtendWith(MockKExtension::class)
internal class ExampleServiceTest {

    private val exampleRepository = mockk<ExampleRepository>()
    @MockK
    private lateinit var exampleMapper: ExampleMapper
    @MockK
    private lateinit var pageConverter: PageConverter
    @MockK
    private lateinit var jsonPlaceholderService: JsonPlaceholderService

    private lateinit var victim: ExampleService

    @BeforeEach
    fun initialize() {
        victim = ExampleService(false, exampleRepository, exampleMapper, pageConverter, jsonPlaceholderService)
    }

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
        val pageDetails = PageDetails(exampleDTOs, 0, 10, 2, 1, true,
            listOf(SortOrder("createdDate", "DESC"), SortOrder("id", "DESC")))
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
        assertEquals(2, actual.sortOrders.size)
        assertEquals("createdDate", actual.sortOrders.first().property)
        assertEquals("DESC", actual.sortOrders.first().direction)
        assertEquals("id", actual.sortOrders.last().property)
        assertEquals("DESC", actual.sortOrders.last().direction)
        assertEquals(id2, actual.content.last().id)
        assertEquals("$id2. example", actual.content.last().name)
        assertFalse(victim.cacheEnabled)
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
        val pageDetails = PageDetails(exampleDTOs, 0, 10, 2, 1, true, listOf(SortOrder("id", "DESC")))
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
        assertFalse(victim.cacheEnabled)
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
        val pageDetails = PageDetails(exampleDTOs, 0, 10, 2, 1, true, listOf(SortOrder("id", "DESC")))
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
        assertFalse(victim.cacheEnabled)
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

    @Test
    fun `Should return word count for users`() {
        // given
        val userId1 = 1L
        val user1 = User(userId1, "name1", "username1", "email1")
        val userId2 = 2L
        val user2 = User(userId2, "name2", "username2", "email2")
        val users = listOf(user1, user2)
        val usersFuture: CompletableFuture<List<User>> = CompletableFuture.completedFuture(users)

        every { jsonPlaceholderService.getUsers() } returns usersFuture

        val posts1 = listOf(
            Post(1, userId1, "title1", "a b c"),
            Post(2, userId1, "title2", "a b c"),
            Post(3, userId1, "title3", "c d e f")
        )
        val posts2 = listOf(
            Post(4, userId2, "title1", "a b c"),
            Post(5, userId2, "title2", "d e f"),
            Post(6, userId2, "title3", "g h i j k l")
        )
        every { jsonPlaceholderService.getPostsByUserId(userId1) } returns CompletableFuture.completedFuture(posts1)
        every { jsonPlaceholderService.getPostsByUserId(userId2) } returns CompletableFuture.completedFuture(posts2)

        // when
        val actual = victim.getWordCountForUsers()

        // then
        assertEquals(users.size, actual.size)
        assertEquals(12, actual[user2.username])
        assertEquals(10, actual[user1.username])
        assertFalse(victim.cacheEnabled)
    }
}
