package com.example.templateproject.core.configuration

import com.example.templateproject.core.service.ExampleService
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@ConditionalOnProperty(value = ["core.database.cache.enabled"], havingValue = "true")
class DatabaseCacheConfiguration(
    @param:Value($$"${core.database.cache.expiration.minutes}") private val expirationMinutes: Long,
    @param:Value($$"${core.database.cache.examples.maxSize}") private val examplesCacheMaxSize: Long,
    private val cacheManager: CaffeineCacheManager,
) {
    @PostConstruct
    fun registerCaches() {
        registerCache(ExampleService.EXAMPLES_CACHE_NAME, examplesCacheMaxSize, expirationMinutes)
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
