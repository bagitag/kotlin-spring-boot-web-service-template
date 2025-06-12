package com.example.templateproject.client.exception

data class ExternalServiceException(
    override val cause: Throwable,
    override val message: String,
    val serviceName: String
) : RuntimeException(message, cause)
