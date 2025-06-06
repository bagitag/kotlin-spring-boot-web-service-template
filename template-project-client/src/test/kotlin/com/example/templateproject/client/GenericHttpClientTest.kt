package com.example.templateproject.client

import com.example.templateproject.client.jsonplaceholder.api.User
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity

@ExtendWith(MockKExtension::class)
internal class GenericHttpClientTest {
    private var victim = GenericHttpClient()

    @Test
    fun `Should return default response when response has no body but it is successful`() {
        // given
        val logPrefix = "logPrefix"
        val request = 1L
        val defaultResponse = listOf<User>()
        val response: ResponseEntity<List<User>> = ResponseEntity.noContent().build()

        // when
        val actual = victim.perform(logPrefix, request, defaultResponse) { response }

        // then
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `Should return default response in case of unexpected response`() {
        // given
        val logPrefix = "logPrefix"
        val request = 1L
        val defaultResponse = listOf<User>()
        val response: ResponseEntity<List<User>> = ResponseEntity.badRequest().build()

        // when
        val actual = victim.perform(logPrefix, request, defaultResponse) { response }

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

        // when
        val actual = victim.perform(logPrefix, request, defaultResponse) { response }

        // then
        assertEquals(body.size, actual.size)
        assertEquals(body[0], actual[0])
    }
}
