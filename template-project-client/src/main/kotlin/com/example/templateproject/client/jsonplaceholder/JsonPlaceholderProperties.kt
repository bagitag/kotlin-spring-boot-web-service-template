package com.example.templateproject.client.jsonplaceholder

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "client.jsonplaceholder")
@Validated
data class JsonPlaceholderProperties
    @ConstructorBinding
    constructor(
        @field:NotEmpty
        val clientId: String,
        @field:NotEmpty
        val baseUrl: String,
        @field:NotEmpty
        val apiKey: String,
        val connectionTimeoutMillis: Long = 1000,
        val readTimeoutMillis: Long = 3000,
        @field:Valid
        val threadPool: ThreadPoolTaskExecutorProperties,
    )

data class ThreadPoolTaskExecutorProperties
    @ConstructorBinding
    constructor(
        @field:NotNull
        val corePoolSize: Int,
        @field:NotNull
        val maxPoolSize: Int,
        @field:NotNull
        val queueCapacity: Int,
    )
