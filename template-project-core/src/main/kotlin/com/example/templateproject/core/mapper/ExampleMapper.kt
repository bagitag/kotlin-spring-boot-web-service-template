package com.example.templateproject.core.mapper

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.persistence.repository.ExampleRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ExampleMapper(repository: ExampleRepository) : AbstractMapper<Example, ExampleDTO>(repository) {

    override fun toEntity(dto: ExampleDTO): Example {
        return Optional.ofNullable(dto.id)
            .map { getEntityId(it) }
            .map { it.apply { name = dto.name } }
            .orElseGet { Example(dto.name) }
    }

    override fun toDTO(entity: Example) = ExampleDTO(
        entity.name
    ).apply {
        setBaseDTOFields(this, entity)
    }
}
