package com.example.templateproject.core.exception

import org.springframework.http.HttpStatus

data class ExternalServiceTimeoutException(
    val clientId: String
) : BaseException("Timeout occurred while communicating with: $clientId", HttpStatus.INTERNAL_SERVER_ERROR)
