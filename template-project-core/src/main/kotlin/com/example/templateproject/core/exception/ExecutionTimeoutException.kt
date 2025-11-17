package com.example.templateproject.core.exception

import org.springframework.http.HttpStatus

class ExecutionTimeoutException(
    val taskDescription: String,
    val details: String?,
) : BaseException("Execution timed out for task: '$taskDescription'", HttpStatus.INTERNAL_SERVER_ERROR)
