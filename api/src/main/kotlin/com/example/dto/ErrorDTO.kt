package com.example.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDTO(
    val id: String,
    val message: String,
    val details: Any?,
    val stackTrace: String?
)
