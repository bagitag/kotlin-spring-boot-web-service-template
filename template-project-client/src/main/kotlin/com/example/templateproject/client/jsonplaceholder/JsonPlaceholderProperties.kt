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
        @NotEmpty
        val clientId: String,
        @NotEmpty
        val baseUrl: String,
        @NotEmpty
        val apiKey: String,
        val connectionTimeoutMillis: Long = 1000,
        val readTimeoutMillis: Long = 3000,
        @Valid
        val threadPool: ThreadPoolTaskExecutorProperties,
    )

data class ThreadPoolTaskExecutorProperties
    @ConstructorBinding
    constructor(
        @NotNull
        val corePoolSize: Int,
        @NotNull
        val maxPoolSize: Int,
        @NotNull
        val queueCapacity: Int,
    )
