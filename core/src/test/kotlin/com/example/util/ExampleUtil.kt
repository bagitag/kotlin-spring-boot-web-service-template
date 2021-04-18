package com.example.util

import com.example.dto.ExampleDTO
import com.example.entity.Example

fun anExample(id: Long) = Example(id, "#$id example")

fun anExampleDTO(id: Long) = ExampleDTO(id, "#$id example")

fun anExampleDTOFromEntity(example: Example) = ExampleDTO(example.id, example.name)