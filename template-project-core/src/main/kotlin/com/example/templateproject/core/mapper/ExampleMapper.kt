package com.example.templateproject.core.mapper

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.persistence.entity.Example
import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Service

@Service
class ExampleMapper {

    @Timed(value = "app.method.executions", extraTags = ["topic", "example-entity-mapping"],
        description = "The amount of time spent in the different methods.")
    fun toDTO(source: Example) = ExampleDTO(
        id = source.id,
        name = source.name,
        createdDate = source.createdDate
    )

    @Timed(value = "app.method.executions", extraTags = ["topic", "example-entity-mapping"],
        description = "The amount of time spent in the different methods.")
    fun fromDTO(source: ExampleDTO) = Example(
        id = source.id,
        name = source.name,
        createdDate = source.createdDate
    )
}
