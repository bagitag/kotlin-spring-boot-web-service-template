package com.example.templateproject.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ExampleDTO(
    @field:NotBlank
    @field:Size(min = 3, max = 20, message = "must be between 3 and 20 characters")
    val name: String,
) : BaseDTO() {
    override fun toString() = "ExampleDTO(name='$name', ${super.toString()})"
}
