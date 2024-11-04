package com.example.templateproject.core.util

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.persistence.entity.Example
import java.time.Instant

fun anExample(id: Long) = Example(id, "$id. example", Instant.now())

fun anExampleDTO(id: Long) = ExampleDTO(id, "$id. example", Instant.now())

fun anExampleDTOFromEntity(example: Example) = ExampleDTO(example.id, example.name)
