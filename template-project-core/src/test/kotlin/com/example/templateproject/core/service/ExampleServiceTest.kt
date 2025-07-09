package com.example.templateproject.core.service

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.api.dto.SortOrder
import com.example.templateproject.client.exception.ExternalServiceException
import com.example.templateproject.client.jsonplaceholder.JsonPlaceholderService
import com.example.templateproject.client.jsonplaceholder.api.Post
import com.example.templateproject.client.jsonplaceholder.api.User
import com.example.templateproject.core.exception.BadRequestErrorMessages
import com.example.templateproject.core.exception.BadRequestException
import com.example.templateproject.core.exception.ExecutionTimeoutException
import com.example.templateproject.core.exception.IdNotFoundException
import com.example.templateproject.core.mapper.ExampleMapper
import com.example.templateproject.core.mapper.PageConverter
import com.example.templateproject.core.util.aMockExample
import com.example.templateproject.core.util.anExampleDTO
import com.example.templateproject.core.util.anExampleDTOFromEntity
import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.persistence.repository.ExampleRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
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
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

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
        victim = ExampleService(false, 10000L, exampleRepository, exampleMapper, jsonPlaceholderService, pageConverter)
    }

    @Test
    fun `Should return paginated entities with default sort`() {
        // given
        val id1 = 100L
        val id2 = 200L
        val example1 = aMockExample(id1)
        val example2 = aMockExample(id2)
        val examples = PageImpl(listOf(example1, example2))
        val pageRequest = PageRequest.of(0, 10)
        val pageable = PageRequest.of(0, 10, AbstractService.DEFAULT_SORT)

        every { exampleRepository.findAll(pageable) } returns examples
        val exampleDTO1 = anExampleDTOFromEntity(example1)
        every { exampleMapper.toDTO(example1) } returns exampleDTO1
        val exampleDTO2 = anExampleDTOFromEntity(example2)
        every { exampleMapper.toDTO(example2) } returns exampleDTO2

        val exampleDTOs = listOf(exampleDTO1, exampleDTO2)
        val pageDetails =
            PageDetails(
                exampleDTOs,
                0,
                10,
                2,
                1,
                true,
                listOf(SortOrder("createdDate", "DESC"), SortOrder("id", "DESC")),
            )
        every { pageConverter.createPageDetails(any<PageImpl<ExampleDTO>>()) } returns pageDetails

        // when
        val actual = victim.getEntities(pageRequest)

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
    fun `Should return paginated entities with custom sort`() {
        // given
        val id1 = 100L
        val id2 = 200L
        val id3 = 300L
        val example1 = aMockExample(id1)
        val example2 = aMockExample(id2)
        val example3 = aMockExample(id3)
        val examples = PageImpl(listOf(example3, example2, example1))
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))

        every { exampleRepository.findAll(pageable) } returns examples
        val exampleDTO3 = anExampleDTOFromEntity(example3)
        every { exampleMapper.toDTO(example3) } returns exampleDTO3
        val exampleDTO2 = anExampleDTOFromEntity(example2)
        every { exampleMapper.toDTO(example2) } returns exampleDTO2
        val exampleDTO1 = anExampleDTOFromEntity(example1)
        every { exampleMapper.toDTO(example1) } returns exampleDTO1

        val exampleDTOs = listOf(exampleDTO3, exampleDTO2, exampleDTO1)
        val pageDetails = PageDetails(exampleDTOs, 0, 10, 2, 1, true, listOf(SortOrder("id", "DESC")))
        every { pageConverter.createPageDetails(any<PageImpl<ExampleDTO>>()) } returns pageDetails

        // when
        val actual = victim.getEntities(pageable)

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
    fun `Should return entities containing search term`() {
        // given
        val id1 = 199L
        val id2 = 399L
        val example1 = aMockExample(id1)
        val example2 = aMockExample(id2)
        val examples = PageImpl(listOf(example2, example1))
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        val searchTerm = listOf("99")

        every { exampleRepository.findByNameInIgnoreCase(searchTerm, pageable) } returns examples
        val exampleDTO2 = anExampleDTOFromEntity(example2)
        every { exampleMapper.toDTO(example2) } returns exampleDTO2
        val exampleDTO1 = anExampleDTOFromEntity(example1)
        every { exampleMapper.toDTO(example1) } returns exampleDTO1

        val exampleDTOs = listOf(exampleDTO1, exampleDTO2)
        val pageDetails = PageDetails(exampleDTOs, 0, 10, 2, 1, true, listOf(SortOrder("id", "DESC")))
        every { pageConverter.createPageDetails(any<PageImpl<ExampleDTO>>()) } returns pageDetails

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
    fun `Should return entity for the given id`() {
        // given
        val id = 8L
        val example = aMockExample(id)

        every { exampleRepository.findById(id) } returns Optional.of(example)
        every { exampleMapper.toDTO(example) } returns anExampleDTOFromEntity(example)

        // when
        val actual = victim.getEntityById(id)

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
        assertThrows<IdNotFoundException> { victim.getEntityById(id) }

        // then
        verifySequence {
            exampleRepository.findById(id)
        }
    }

    @Test
    fun `Creating new entity with id should throw exception`() {
        // given
        val dto = anExampleDTO(100L)

        // when
        val actual = assertThrows<BadRequestException> { victim.createEntity(dto) }

        // then
        assertEquals(BadRequestErrorMessages.ID_MUST_BE_NULL.message, actual.reason.message)
    }

    @Test
    fun `Creating new invalid entity should throw exception`() {
        // given
        val name = "invalid example"
        val dto = ExampleDTO(name)
        val entity = Example(name)
        every { exampleMapper.toEntity(dto) } returns entity

        // when
        val actual = assertThrows<BadRequestException> { victim.createEntity(dto) }

        // then
        assertEquals(BadRequestErrorMessages.NAME_MUST_START_WITH_A_NUMBER.message, actual.reason.message)
    }

    @Test
    fun `Should create new example and return it`() {
        // given
        val id = 99L
        val name = "$id. example"
        val requestExampleDTO = ExampleDTO(name)
        val example = Example(name)
        val responseExampleDTO = ExampleDTO(name).apply { this.id = id }

        every { exampleMapper.toEntity(requestExampleDTO) } returns example
        every { exampleRepository.save(example) } returns example
        every { exampleMapper.toDTO(example) } returns responseExampleDTO

        // when
        val actual = victim.createEntity(requestExampleDTO)

        // then
        verifySequence {
            exampleMapper.toEntity(requestExampleDTO)
            exampleRepository.save(example)
            exampleMapper.toDTO(example)
        }
        assertEquals(responseExampleDTO, actual)
    }

    @Test
    fun `Updating entity without id should throw exception`() {
        // given
        val dto = ExampleDTO("name")

        // when
        val actual = assertThrows<BadRequestException> { victim.updateEntity(dto) }

        // then
        assertEquals(BadRequestErrorMessages.ID_MUST_NOT_BE_NULL.message, actual.reason.message)
    }

    @Test
    fun `Updating entity should throw IdNotFoundException`() {
        // given
        val id = 100L
        val name = "test example"
        val exampleDTO = ExampleDTO(name).apply { this.id = id }

        every { exampleRepository.findById(id) } returns Optional.empty()

        // when
        assertThrows<IdNotFoundException> { victim.updateEntity(exampleDTO) }

        // then
        verifySequence {
            exampleRepository.findById(id)
        }
    }

    @Test
    fun `Should update existing example and return it`() {
        // given
        val id = 99L
        val name = "$id. example"
        val requestExampleDTO = ExampleDTO(name).apply { this.id = id }
        val example = Example(name)
        val responseExampleDTO = ExampleDTO(name).apply { this.id = id }

        every { exampleRepository.findById(id) } returns Optional.of(example)
        every { exampleMapper.toEntity(requestExampleDTO) } returns example
        every { exampleRepository.save(example) } returns example
        every { exampleMapper.toDTO(example) } returns responseExampleDTO

        // when
        val actual = victim.updateEntity(requestExampleDTO)

        // then
        verifySequence {
            exampleRepository.findById(id)
            exampleMapper.toEntity(requestExampleDTO)
            exampleRepository.save(example)
            exampleMapper.toDTO(example)
        }
        assertEquals(responseExampleDTO, actual)
    }

    @Test
    fun `Should delete example`() {
        // given
        val id = 99L

        every { exampleRepository.deleteById(id) } returns mockk()

        // when
        victim.deleteEntity(id)

        // then
        verifySequence {
            exampleRepository.deleteById(id)
        }
    }

    @Test
    fun `Should throw timeout exception`() {
        // given
        victim = ExampleService(false, 1000L, exampleRepository, exampleMapper, jsonPlaceholderService, pageConverter)

        every { jsonPlaceholderService.clientId } returns "clientId"

        every { jsonPlaceholderService.getUsers() } returns
            CompletableFuture.supplyAsync({ listOf() }, CompletableFuture.delayedExecutor(2L, TimeUnit.SECONDS))

        // when - then
        val exception = assertThrows<ExecutionTimeoutException> { victim.getWordCountForUsers() }
        assertEquals("Calculating word count for users", exception.taskDescription)
    }

    @Test
    fun `Should handle unknown ExecutionException`() {
        // given
        victim = ExampleService(false, 1000L, exampleRepository, exampleMapper, jsonPlaceholderService, pageConverter)

        val clientId = "clientId"
        every { jsonPlaceholderService.clientId } returns clientId

        val exceptionMsg = "NPE message"
        every { jsonPlaceholderService.getUsers() } throws
                ExecutionException("NullPointerException", NullPointerException(exceptionMsg))

        // when - then
        val exception = assertThrows<ExternalServiceException> { victim.getWordCountForUsers() }
        assertEquals(exceptionMsg, exception.message)
        assertEquals(clientId, exception.serviceName)
    }

    @Test
    fun `Should handle ExecutionException`() {
        // given
        victim = ExampleService(false, 1000L, exampleRepository, exampleMapper, jsonPlaceholderService, pageConverter)

        val clientId = "clientId"
        every { jsonPlaceholderService.clientId } returns clientId

        val cause = HttpClientErrorException(HttpStatus.BAD_REQUEST)
        every { jsonPlaceholderService.getUsers() } throws
                ExecutionException(ExternalServiceException(cause, "Bad Request", clientId))

        // when - then
        val exception = assertThrows<ExternalServiceException> { victim.getWordCountForUsers() }
        assertEquals(cause, exception.cause)
        assertEquals(HttpStatus.BAD_REQUEST, (exception.cause as HttpClientErrorException).statusCode)
        assertEquals(clientId, exception.serviceName)
    }

    @Test
    fun `Should return map with zero word count`() {
        // given
        val userId1 = 1L
        val user1 = User(userId1, "name1", "username1", "email1")
        val users = listOf(user1)
        val usersFuture: CompletableFuture<List<User>> = CompletableFuture.completedFuture(users)

        every { jsonPlaceholderService.getUsers() } returns usersFuture

        every { jsonPlaceholderService.getPostsByUserId(userId1) } returns CompletableFuture.completedFuture(listOf())

        // when
        val actual = victim.getWordCountForUsers()

        // then
        assertEquals(users.size, actual.size)
        assertEquals(0, actual[user1.username])
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

        val posts1 =
            listOf(
                Post(1, userId1, "title1", "a b c"),
                Post(2, userId1, "title2", "a b c"),
                Post(3, userId1, "title3", "c d e f"),
            )
        val posts2 =
            listOf(
                Post(4, userId2, "title1", "a b c"),
                Post(5, userId2, "title2", "d e f"),
                Post(6, userId2, "title3", "g h i j k l"),
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
