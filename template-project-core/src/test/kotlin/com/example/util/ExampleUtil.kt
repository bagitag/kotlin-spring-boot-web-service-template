package com.example.util

import com.example.dto.ExampleDTO
import com.example.entity.Example
import java.time.Instant

fun anExample(id: Long) = Example(id, "$id. example", Instant.now())

fun anExampleDTO(id: Long) = ExampleDTO(id, "$id. example", Instant.now())

fun anExampleDTOFromEntity(example: Example) = ExampleDTO(example.id, example.name)
