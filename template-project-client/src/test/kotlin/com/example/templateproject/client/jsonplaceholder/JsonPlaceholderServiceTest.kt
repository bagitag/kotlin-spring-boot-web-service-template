package com.example.templateproject.client.jsonplaceholder

import com.example.templateproject.client.GenericHttpClient
import com.example.templateproject.client.RetryableHttpRequestDecorator
import com.example.templateproject.client.jsonplaceholder.api.Post
import com.example.templateproject.client.jsonplaceholder.api.User
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import java.util.concurrent.ExecutionException

@ExtendWith(MockKExtension::class)
internal class JsonPlaceholderServiceTest {

    @MockK
    private lateinit var jsonPlaceholderClient: JsonPlaceholderClient
    @MockK
    private lateinit var httpClient: GenericHttpClient
    @MockK
    private lateinit var retryDecorator: RetryableHttpRequestDecorator
    @MockK
    private lateinit var circuitBreaker: JsonPlaceholderCircuitBreaker

    private lateinit var victim: JsonPlaceholderService

    @BeforeEach
    fun initialize() {
        victim = JsonPlaceholderService(false, jsonPlaceholderClient, httpClient, retryDecorator, circuitBreaker)
    }

    @Test
    fun `Get users should return user list`() {
        // given
        val body = listOf(
            User(1, "1", "username1", "email1"),
            User(2, "2", "username2", "email2"),
            User(3, "3", "username3", "email3")
        )

        every { jsonPlaceholderClient.getUsers() } returns ResponseEntity.ok(body)

        every {
            retryDecorator.retryForHttpServerError<List<User>>(any(), any())
        } answers { secondArg<() -> List<User>>().invoke() }

        every {
            httpClient.perform<List<User>>(any(), any(), any(), any())
        } returns body

        every {
            circuitBreaker.decorate<List<User>>(any())
        } answers { firstArg<() -> List<User>>().invoke() }

        // when
        val actual = victim.getUsers()

        // then
        assertEquals(body.size, actual.get().size)
        assertEquals(body[0].id, actual.get()[0].id)
        assertEquals(body[0].name, actual.get()[0].name)
        assertEquals(body[0].username, actual.get()[0].username)
        assertEquals(body[0].email, actual.get()[0].email)
        assertFalse(victim.cacheEnabled)
    }

    @Test
    fun `Get users should return error`() {
        // given
        every { jsonPlaceholderClient.getUsers() } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            retryDecorator.retryForHttpServerError<List<User>>(any(), any())
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            circuitBreaker.decorate { any<Any>() }
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            httpClient.perform(
                any<String>(),
                any<String>(),
                any<List<User>>()
            ) { any() }
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        // when
        val actual = assertThrows<ExecutionException> { victim.getUsers().get() }

        // then
        assertTrue(actual.cause is HttpServerErrorException)
    }

    @Test
    fun `Get posts by user id should return post list`() {
        // given
        val userId = 10L
        val body = listOf(
            Post(1, userId, "title1", "body1"),
            Post(2, userId, "title2", "body2"),
            Post(3, userId, "title3", "body3")
        )

        every { jsonPlaceholderClient.getAllPostByUserId(userId) } returns ResponseEntity.ok(body)

        every {
            retryDecorator.retryForHttpServerError<List<Post>>(any(), any())
        } answers { secondArg<() -> List<Post>>().invoke() }

        every {
            httpClient.perform<List<Post>>(any(), any(), any(), any())
        } returns body

        every {
            circuitBreaker.decorate<List<User>>(any())
        } answers { firstArg<() -> List<User>>().invoke() }

        // when
        val actual = victim.getPostsByUserId(userId)

        // then
        assertEquals(body.size, actual.get().size)
        assertEquals(body[0].id, actual.get()[0].id)
        assertEquals(body[0].userId, actual.get()[0].userId)
        assertEquals(body[0].title, actual.get()[0].title)
        assertEquals(body[0].body, actual.get()[0].body)
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
            retryDecorator.retryForHttpServerError<List<Post>>(any(), any())
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            circuitBreaker.decorate<List<Post>> { any() }
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            httpClient.perform(
                any<String>(),
                any<String>(),
                any<List<Post>>()
            ) { any() }
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        // when
        val actual = assertThrows<ExecutionException> { victim.getPostsByUserId(userId).get() }

        // then
        assertTrue(actual.cause is HttpServerErrorException)
    }

    @Test
    fun `Should return default response in case of HttpClientErrorException`() {
        // given
        every { jsonPlaceholderClient.getUsers() } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        every {
            retryDecorator.retryForHttpServerError<List<User>>(any(), any())
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        every {
            circuitBreaker.decorate { any<Any>() }
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        every {
            httpClient.perform(
                any<String>(),
                any<String>(),
                any<List<User>>()
            ) { any() }
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        // when
        val actual = assertThrows<ExecutionException> { victim.getUsers().get() }

        // then
        assertTrue(actual.cause is HttpClientErrorException)
    }
}
