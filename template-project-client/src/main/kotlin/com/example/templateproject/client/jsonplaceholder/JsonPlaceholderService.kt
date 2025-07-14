package com.example.templateproject.client.jsonplaceholder

import com.example.templateproject.client.GenericHttpClient
import com.example.templateproject.client.RetryableHttpRequestDecorator
import com.example.templateproject.client.jsonplaceholder.api.Post
import com.example.templateproject.client.jsonplaceholder.api.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class JsonPlaceholderService(
    @param:Value($$"${client.jsonplaceholder.client-id:asd}") val clientId: String,
    @param:Value($$"${client.jsonplaceholder.cache.enabled}") val cacheEnabled: Boolean,
    private val jsonPlaceholderClient: JsonPlaceholderClient,
    private val httpClient: GenericHttpClient,
    private val retryDecorator: RetryableHttpRequestDecorator,
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

    private fun <T> execute(
        request: Any,
        defaultResponse: T,
        httpCall: () -> ResponseEntity<T>,
    ): CompletableFuture<T> =
        CompletableFuture.completedFuture(
            retryDecorator.retryForHttpServerError(request) {
                circuitBreaker.decorate {
                    httpClient.perform(clientId, request, defaultResponse, httpCall)
                }
            },
        )
}
