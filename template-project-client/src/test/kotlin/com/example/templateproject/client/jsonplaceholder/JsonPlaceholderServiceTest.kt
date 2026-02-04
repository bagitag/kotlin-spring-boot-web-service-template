package com.example.templateproject.client.jsonplaceholder

import com.example.templateproject.client.GenericHttpClient
import com.example.templateproject.client.jsonplaceholder.api.Post
import com.example.templateproject.client.jsonplaceholder.api.User
import com.example.templateproject.client.jsonplaceholder.configuration.JsonPlaceholderCircuitBreaker
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.retry.RetryTemplate
import org.springframework.core.retry.Retryable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpServerErrorException

@ExtendWith(MockKExtension::class)
internal class JsonPlaceholderServiceTest {
    @MockK
    private lateinit var jsonPlaceholderClient: JsonPlaceholderClient

    @MockK
    private lateinit var httpClient: GenericHttpClient

    @MockK
    private lateinit var retryTemplate: RetryTemplate

    @MockK
    private lateinit var circuitBreaker: JsonPlaceholderCircuitBreaker

    private lateinit var victim: JsonPlaceholderService

    @BeforeEach
    fun initialize() {
        victim =
            JsonPlaceholderService("clientId", false, jsonPlaceholderClient, httpClient, retryTemplate, circuitBreaker)
    }

    @Test
    fun `Get users should return user list`() {
        // given
        val body =
            listOf(
                User(1, "1", "username1", "email1"),
                User(2, "2", "username2", "email2"),
                User(3, "3", "username3", "email3"),
            )

        every { jsonPlaceholderClient.getUsers() } returns ResponseEntity.ok(body)

        every {
            retryTemplate.execute<List<User>>(any())
        } answers {
            firstArg<Retryable<List<User>>>().execute()
        }

        every {
            httpClient.perform<List<User>>(any(), any(), any(), any())
        } returns body

        every {
            circuitBreaker.decorate<List<User>>(any())
        } answers {
            firstArg<() -> List<User>>().invoke()
        }

        // when
        val actual = victim.getUsers()

        // then
        assertEquals(body.size, actual.get().size)
        body.forEachIndexed { index, user ->
            assertEquals(user.id, actual.get()[index].id)
            assertEquals(user.name, actual.get()[index].name)
            assertEquals(user.username, actual.get()[index].username)
            assertEquals(user.email, actual.get()[index].email)
        }
        assertFalse(victim.cacheEnabled)
    }

    @Test
    fun `Get users should return error`() {
        // given
        every { jsonPlaceholderClient.getUsers() } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            retryTemplate.execute<List<User>>(any())
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            circuitBreaker.decorate { any<Any>() }
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            httpClient.perform(
                any<String>(),
                any<String>(),
                any<List<User>>(),
            ) { any() }
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        // when - then
        assertThrows<HttpServerErrorException> { victim.getUsers().get() }
    }

    @Test
    fun `Get posts by user id should return post list`() {
        // given
        val userId = 10L
        val body =
            listOf(
                Post(1, userId, "title1", "body1"),
                Post(2, userId, "title2", "body2"),
                Post(3, userId, "title3", "body3"),
            )

        every { jsonPlaceholderClient.getAllPostByUserId(userId) } returns ResponseEntity.ok(body)

        every {
            retryTemplate.execute<List<Post>>(any())
        } answers {
            firstArg<Retryable<List<Post>>>().execute()
        }

        every {
            httpClient.perform<List<Post>>(any(), any(), any(), any())
        } returns body

        every {
            circuitBreaker.decorate<List<User>>(any())
        } answers {
            firstArg<() -> List<User>>().invoke()
        }

        // when
        val actual = victim.getPostsByUserId(userId)

        // then
        assertEquals(body.size, actual.get().size)
        body.forEachIndexed { index, post ->
            assertEquals(post.id, actual.get()[index].id)
            assertEquals(post.userId, actual.get()[index].userId)
            assertEquals(post.title, actual.get()[index].title)
            assertEquals(post.body, actual.get()[index].body)
        }
        assertFalse(victim.cacheEnabled)
    }

    @Test
    fun `Get posts by user id should return error`() {
        // given
        val userId = 1L
        every {
            jsonPlaceholderClient.getAllPostByUserId(userId)
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            retryTemplate.execute<List<Post>>(any())
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            circuitBreaker.decorate<List<Post>> { any() }
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            httpClient.perform(
                any<String>(),
                any<String>(),
                any<List<Post>>(),
            ) { any() }
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        // when - then
        assertThrows<HttpServerErrorException> { victim.getPostsByUserId(userId).get() }
    }

    @Test
    fun `Retryable getName returns expected value`() {
        // given
        val retryable = victim.retryable { "result" }

        // when - then
        assertEquals("JsonPlaceholderServiceRetryable", retryable.name)
    }
}
