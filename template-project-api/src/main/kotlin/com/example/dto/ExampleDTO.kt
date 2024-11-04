package com.example.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class ExampleDTO(
    val id: Long? = null,
    @field:NotBlank
    @field:Size(min = 3, max = 20, message = "must be between 3 and 20 characters")
    val name: String,
    var createdDate: Instant? = null
)
