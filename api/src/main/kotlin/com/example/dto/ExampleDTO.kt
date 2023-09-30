package com.example.dto

import java.time.Instant

data class ExampleDTO(
    val id: Long? = null,
    val name: String,
    var createdDate: Instant? = null
)
