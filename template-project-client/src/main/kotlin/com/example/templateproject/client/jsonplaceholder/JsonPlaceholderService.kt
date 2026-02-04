package com.example.templateproject.client.jsonplaceholder

import com.example.templateproject.client.GenericHttpClient
import com.example.templateproject.client.jsonplaceholder.api.Post
import com.example.templateproject.client.jsonplaceholder.api.User
import com.example.templateproject.client.jsonplaceholder.configuration.JsonPlaceholderCacheConfiguration
import com.example.templateproject.client.jsonplaceholder.configuration.JsonPlaceholderCircuitBreaker
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.retry.RetryTemplate
import org.springframework.core.retry.Retryable
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class JsonPlaceholderService(
    @param:Value($$"${client.jsonplaceholder.client-id}") val clientId: String,
    @param:Value($$"${client.jsonplaceholder.cache.enabled}") val cacheEnabled: Boolean,
    private val jsonPlaceholderClient: JsonPlaceholderClient,
    private val httpClient: GenericHttpClient,
    private val retryTemplateForHttpServerError: RetryTemplate,
    private val circuitBreaker: JsonPlaceholderCircuitBreaker,
) {
    @Cacheable(
        JsonPlaceholderCacheConfiguration.USERS_CACHE_NAME,
        condition = "#root.target.cacheEnabled",
    )
    @Async("jsonPlaceHolderExecutor")
    fun getUsers(): CompletableFuture<List<User>> {
        val request = USERS_ENDPOINT
        return execute(request, listOf()) { jsonPlaceholderClient.getUsers() }
    }

    @Async("jsonPlaceHolderExecutor")
    fun getPostsByUserId(userId: Long): CompletableFuture<List<Post>> {
        val request = "$POSTS_ENDPOINT?userId=$userId"
        return execute(request, listOf()) { jsonPlaceholderClient.getAllPostByUserId(userId) }
    }

    private fun <T : Any> execute(
        request: Any,
        defaultResponse: T,
        httpCall: () -> ResponseEntity<T>,
    ): CompletableFuture<T> =
        CompletableFuture.completedFuture(
            retryTemplateForHttpServerError.execute(
                retryable {
                    circuitBreaker.decorate {
                        httpClient.perform(clientId, request, defaultResponse, httpCall)
                    }
                },
            ),
        )

    internal fun <T> retryable(method: () -> T) =
        object : Retryable<T> {
            override fun execute(): T = method()

            override fun getName() = "JsonPlaceholderServiceRetryable"
        }
}
