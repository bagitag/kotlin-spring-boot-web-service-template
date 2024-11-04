package com.example.mapper

import com.example.dto.ExampleDTO
import com.example.entity.Example
import org.springframework.stereotype.Service

@Service
class ExampleMapper {
    fun toDTO(source: Example) = ExampleDTO(
        id = source.id,
        name = source.name,
        createdDate = source.createdDate
    )

    fun fromDTO(source: ExampleDTO) = Example(
        id = source.id,
        name = source.name,
        createdDate = source.createdDate
    )
}
