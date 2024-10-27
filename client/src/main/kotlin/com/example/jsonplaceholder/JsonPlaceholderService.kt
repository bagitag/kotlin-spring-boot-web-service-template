package com.example.jsonplaceholder

import com.example.jsonplaceholder.api.Post
import com.example.jsonplaceholder.api.User
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

@Service
class JsonPlaceholderService(
    private var jsonPlaceholderClient: JsonPlaceholderClient,
    private var httpClient: GenericHttpClient,
    private var retryDecorator: RetryableHttpRequestDecorator,
    private var circuitBreaker: JsonPlaceholderCircuitBreaker
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JsonPlaceholderService::class.java)
        private const val SERVICE_NAME = "JSON_PLACEHOLDER"
        private const val LOG_PREFIX = "[$SERVICE_NAME]"
    }

    fun getUsers(): CompletableFuture<List<User>> {
        val request = USERS_ENDPOINT
        return execute(request, listOf()) { jsonPlaceholderClient.getUsers() }
    }

    fun getPostsByUserId(userId: Long): CompletableFuture<List<Post>> {
        val request = "$POSTS_ENDPOINT?userId=$userId"
        return execute(request, listOf()) { jsonPlaceholderClient.getAllPostByUserId(userId) }
    }

    private fun <T> execute(
        request: Any,
        defaultResponse: T,
        httpCall: () -> ResponseEntity<T>
    ): CompletableFuture<T> {
        return CompletableFuture.supplyAsync {
            retryDecorator.retryForHttpServerError(request) {
                circuitBreaker.decorate {
                    httpClient.perform(LOG_PREFIX, request, defaultResponse, httpCall)
                }
            }
        }.exceptionally { ex ->
            handleException(ex)
            defaultResponse
        }
    }

    private fun handleException(ex: Throwable) {
        val cause = ex.cause!!

        when (cause) {
            is HttpClientErrorException -> {
                val oneLineBody =
                    cause.responseBodyAsString.lines().stream().map(String::trim).collect(Collectors.joining())
                LOGGER.error(
                    "$LOG_PREFIX - Client side error: {} - Response body: {}",
                    cause.statusText,
                    oneLineBody
                )
            }
        }
        throw cause
    }
}
