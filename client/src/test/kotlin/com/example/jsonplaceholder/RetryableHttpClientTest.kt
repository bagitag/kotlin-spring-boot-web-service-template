package com.example.jsonplaceholder

import com.example.jsonplaceholder.api.User
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import java.util.function.Supplier

@ExtendWith(MockKExtension::class)
internal class RetryableHttpClientTest {

    private var victim = RetryableHttpClient()

    @Test
    fun `Should return default response when response has no body but it is successful`() {
        // given
        val logPrefix = "logPrefix"
        val request = 1L
        val defaultResponse = listOf<User>()
        val response : ResponseEntity<List<User>> = ResponseEntity.noContent().build()
        val supplier = Supplier { response }

        // when
        val actual = victim.retryForHttpServerError(logPrefix, request, defaultResponse, supplier)

        // then
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `Should return default response in case of unexpected response`() {
        // given
        val logPrefix = "logPrefix"
        val request = 1L
        val defaultResponse = listOf<User>()
        val response : ResponseEntity<List<User>> = ResponseEntity.badRequest().build()
        val supplier = Supplier { response }

        // when
        val actual = victim.retryForHttpServerError(logPrefix, request, defaultResponse, supplier)

        // then
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `Should return user list`() {
        // given
        val logPrefix = "logPrefix"
        val request = 1L
        val defaultResponse = listOf<User>()
        val body = listOf(User(1L, "name", "username", "email"))
        val response = ResponseEntity.ok(body)
        val supplier = Supplier { response }

        // when
        val actual = victim.retryForHttpServerError(logPrefix, request, defaultResponse, supplier)

        // then
        assertEquals(body.size, actual.size)
        assertEquals(body[0], actual[0])
    }
}
