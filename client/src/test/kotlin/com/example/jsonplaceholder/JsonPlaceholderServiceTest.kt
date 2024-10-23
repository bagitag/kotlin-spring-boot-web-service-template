package com.example.jsonplaceholder

import com.example.exception.ExternalServiceException
import com.example.jsonplaceholder.api.Post
import com.example.jsonplaceholder.api.User
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import java.util.concurrent.ExecutionException
import java.util.function.Supplier

@ExtendWith(value = [ MockKExtension::class, OutputCaptureExtension::class ])
internal class JsonPlaceholderServiceTest {

    @MockK
    private lateinit var jsonPlaceholderClient: JsonPlaceholderClient

    @MockK
    private lateinit var retryableClient: RetryableHttpClient

    private lateinit var victim: JsonPlaceholderService

    @BeforeEach
    fun initialize() {
        victim = JsonPlaceholderService(jsonPlaceholderClient, retryableClient)
    }

    @Test
    fun `Get users should return user list`() {
        // given
        val body = listOf(
            User(1, "1", "username1", "email1"),
            User(2, "2", "username2", "email2"),
            User(3, "3", "username3", "email3")
        )

        every {
            retryableClient.retryForHttpServerError(
                any<String>(),
                any<String>(),
                any<List<User>>(),
                any<Supplier<ResponseEntity<List<User>>>>()
            )
        } returns body

        // when
        val actual = victim.getUsers()

        // then
        assertEquals(body.size, actual.get().size)
        assertEquals(body[0].id, actual.get()[0].id)
        assertEquals(body[0].name, actual.get()[0].name)
        assertEquals(body[0].username, actual.get()[0].username)
        assertEquals(body[0].email, actual.get()[0].email)
    }

    @Test
    fun `Get users should thrown exception in case of communication error`() {
        // given
        every { jsonPlaceholderClient.getUsers() } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            retryableClient.retryForHttpServerError(
                any<String>(),
                any<String>(),
                any<List<User>>(),
                any<Supplier<ResponseEntity<List<User>>>>()
            )
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        // when
        val actual = assertThrows<ExecutionException> {  victim.getUsers().get() }

        // then
        assertTrue(actual.cause is ExternalServiceException)
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

        every {
            retryableClient.retryForHttpServerError(
                any<String>(),
                any<String>(),
                any<List<Post>>(),
                any<Supplier<ResponseEntity<List<Post>>>>()
            )
        } returns body

        // when
        val actual = victim.getPostsByUserId(userId)

        // then
        assertEquals(body.size, actual.get().size)
        assertEquals(body[0].id, actual.get()[0].id)
        assertEquals(body[0].userId, actual.get()[0].userId)
        assertEquals(body[0].title, actual.get()[0].title)
        assertEquals(body[0].body, actual.get()[0].body)
    }

    @Test
    fun `Get posts by user id should throw exception in case of error`() {
        // given
        val userId = 1L
        every { jsonPlaceholderClient.getAllPostByUserId(userId) } throws
                HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        every {
            retryableClient.retryForHttpServerError(
                any<String>(),
                any<String>(),
                any<List<Post>>(),
                any<Supplier<ResponseEntity<List<Post>>>>()
            )
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        // when
        val actual = assertThrows<ExecutionException> {  victim.getUsers().get() }

        // then
        assertTrue(actual.cause is ExternalServiceException)
    }

    @Test
    fun `Should return log details in case of HttpClientErrorException`(output: CapturedOutput) {
        // given
        every { jsonPlaceholderClient.getUsers() } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        every {
            retryableClient.retryForHttpServerError(
                any<String>(),
                any<String>(),
                any<List<User>>(),
                any<Supplier<ResponseEntity<List<User>>>>()
            )
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        // when
        val actual = assertThrows<ExecutionException> {  victim.getUsers().get() }

        // then
        assertTrue(actual.cause is ExternalServiceException)
        assertTrue(output.out.contains("[JSON_PLACEHOLDER] - Client side error: "))
    }
}
