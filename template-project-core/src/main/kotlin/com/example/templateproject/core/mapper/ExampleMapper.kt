package com.example.templateproject.core.mapper

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.persistence.entity.Example
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
