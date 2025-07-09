package com.example.templateproject.client

import com.example.templateproject.client.exception.ExternalServiceException
import com.example.templateproject.client.jsonplaceholder.api.User
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException

@ExtendWith(MockKExtension::class)
internal class GenericHttpClientTest {

    private var victim = GenericHttpClient()

    @Test
    fun `Should return default response when response has no body`() {
        // given
        val clientId = "clientId"
        val request = 1L
        val defaultResponse = listOf<User>()
        val response : ResponseEntity<List<User>> = ResponseEntity.noContent().build()

        // when
        val actual = victim.perform(clientId, request, defaultResponse) { response }

        // then
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `Should return default response in case of unexpected response`() {
        // given
        val clientId = "clientId"
        val request = 1L
        val defaultResponse = listOf<User>()
        val response : ResponseEntity<List<User>> = ResponseEntity.badRequest().build()

        // when
        val actual = victim.perform(clientId, request, defaultResponse) { response }

        // then
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `Should throw external service exception`() {
        // given
        val clientId = "clientId"
        val request = 1L
        val defaultResponse = listOf<User>()

        // when - then
        assertThrows<ExternalServiceException> {
            victim.perform(clientId, request, defaultResponse) {
                throw NullPointerException()
            }
        }
    }

    @Test
    fun `Should handle HttpStatusCodeException`() {
        // given
        val clientId = "clientId"
        val request = 1L
        val defaultResponse = listOf<User>()

        // when - then
        assertThrows<ExternalServiceException> {
            victim.perform(clientId, request, defaultResponse) {
                throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request")
            }
        }
    }

    @Test
    fun `Should handle ResourceAccessException`() {
        // given
        val clientId = "clientId"
        val request = 1L
        val defaultResponse = listOf<User>()

        // when - then
        assertThrows<ExternalServiceException> {
            victim.perform(clientId, request, defaultResponse) {
                throw ResourceAccessException("Resource Access Error")
            }
        }
    }

    @Test
    fun `Should handle ResourceAccessException with null message`() {
        // given
        val clientId = "clientId"
        val request = 1L
        val defaultResponse = listOf<User>()

        // when - then
        assertThrows<ExternalServiceException> {
            victim.perform(clientId, request, defaultResponse) {
                throw ResourceAccessException(null)
            }
        }
    }

    @Test
    fun `Should return response`() {
        // given
        val clientId = "clientId"
        val request = 1L
        val defaultResponse = listOf<User>()
        val body = listOf(User(1L, "name", "username", "email"))
        val response = ResponseEntity.ok(body)

        // when
        val actual = victim.perform(clientId, request, defaultResponse) { response }

        // then
        assertEquals(body.size, actual.size)
        assertEquals(body[0], actual[0])
    }
}
