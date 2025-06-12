package com.example.templateproject.core.mapper

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.persistence.entity.Example
import io.micrometer.core.annotation.Timed
import com.example.templateproject.persistence.repository.ExampleRepository
import io.micrometer.tracing.annotation.NewSpan
import io.micrometer.tracing.annotation.SpanTag
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ExampleMapper(repository: ExampleRepository) : AbstractMapper<Example, ExampleDTO>(repository) {

    @Timed(value = "app.method.executions", extraTags = ["topic", "example-entity-mapping"],
        description = "The amount of time spent in the different methods.")
    @NewSpan("dto-to-entity-mapping")
    override fun toEntity(@SpanTag("dto") dto: ExampleDTO): Example {
        return Optional.ofNullable(dto.id)
            .map { getEntityId(it) }
            .map { it.apply { name = dto.name } }
            .orElseGet { Example(dto.name) }
    }

    @Timed(value = "app.method.executions", extraTags = ["topic", "example-entity-mapping"],
        description = "The amount of time spent in the different methods.")
    @NewSpan("entity-to-dto-mapping")
    override fun toDTO(@SpanTag("entity") entity: Example) = ExampleDTO(
        entity.name
    ).apply {
        setBaseDTOFields(this, entity)
    }
}
