package com.example.templateproject.web.configuration

import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheManagerConfiguration {
    @Bean
    fun caffeineCacheManager() = CaffeineCacheManager()
}
