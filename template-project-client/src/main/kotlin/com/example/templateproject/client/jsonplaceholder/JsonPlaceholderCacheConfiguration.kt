package com.example.templateproject.client.jsonplaceholder

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@ConditionalOnProperty(value = ["client.jsonplaceholder.cache.enabled"], havingValue = "true")
class JsonPlaceholderCacheConfiguration(
    @param:Value($$"${client.jsonplaceholder.cache.expiration.minutes}") private val expirationMinutes: Long,
    @param:Value($$"${client.jsonplaceholder.cache.users.maxSize:1}") private val usersCacheMaxSize: Long,
    private val cacheManager: CaffeineCacheManager,
) {
    companion object {
        const val USERS_CACHE_NAME = "jsonplaceholder-users"
    }

    @PostConstruct
    fun registerCaches() {
        registerCache(USERS_CACHE_NAME, usersCacheMaxSize, expirationMinutes)
    }

    private fun registerCache(
        name: String,
        maximumSize: Long,
        expirationMinutes: Long,
    ) {
        cacheManager.registerCustomCache(name, caffeineAsyncCache(maximumSize, expirationMinutes))
    }

    private fun caffeineAsyncCache(
        size: Long,
        expirationMinutes: Long,
    ): AsyncCache<Any, Any> =
        Caffeine
            .newBuilder()
            .expireAfterWrite(expirationMinutes, TimeUnit.MINUTES)
            .maximumSize(size)
            .recordStats()
            .buildAsync()
}
