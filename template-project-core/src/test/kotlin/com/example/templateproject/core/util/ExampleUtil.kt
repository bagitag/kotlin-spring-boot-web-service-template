package com.example.templateproject.core.util

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.persistence.entity.Example
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.LocalDateTime

fun aMockExample(id: Long) : Example {
    val entity = mock(Example::class.java)
    `when`(entity.id).thenReturn(id)
    `when`(entity.name).thenReturn("$id. example")
    `when`(entity.createdDate).thenReturn(Instant.now(DEFAULT_CLOCK))
    `when`(entity.modifiedDate).thenReturn(Instant.now(DEFAULT_CLOCK))
    return entity
}

fun anExampleDTO(id: Long) = ExampleDTO("$id. example").apply {
    this.id = id
    this.createdDate = LocalDateTime.now(DEFAULT_CLOCK)
}

fun anExampleDTOFromEntity(example: Example) = ExampleDTO(example.name).apply {
    this.id = example.id
    this.createdDate = LocalDateTime.ofInstant(example.createdDate, DEFAULT_CLOCK.zone)
    this.modifiedDate = LocalDateTime.ofInstant(example.modifiedDate, DEFAULT_CLOCK.zone)
}
