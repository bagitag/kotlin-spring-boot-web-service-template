package com.example.templateproject.client

import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "client.retry")
@Validated
data class RetryProperties
    @ConstructorBinding
    constructor(
        @field:NotNull
        val maximumRetries: Long,
        val timeout: Long = 3000L,
        val delay: DelayProperties,
    )

@Validated
data class DelayProperties
    @ConstructorBinding
    constructor(
        @field:NotNull
        var millis: Long,
        var multiplier: Double = 1.0,
        var maximumMillis: Long = 1000L,
    )
