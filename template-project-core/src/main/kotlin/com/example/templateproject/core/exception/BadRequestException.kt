package com.example.templateproject.core.exception

import org.springframework.http.HttpStatus

data class BadRequestException(
    val reason: BadRequestErrorMessages,
) : BaseException("Invalid request: ${reason.message}", HttpStatus.BAD_REQUEST)
