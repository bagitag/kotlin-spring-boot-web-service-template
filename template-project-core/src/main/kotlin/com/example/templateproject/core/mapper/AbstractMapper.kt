package com.example.templateproject.core.mapper

import com.example.templateproject.api.dto.BaseDTO
import com.example.templateproject.core.exception.IdNotFoundException
import com.example.templateproject.persistence.entity.BaseEntity
import com.example.templateproject.persistence.entity.Example
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

abstract class AbstractMapper<E : BaseEntity, D : BaseDTO>(
    open val repository: JpaRepository<E, Long>,
) {
    abstract fun toEntity(dto: D): E

    abstract fun toDTO(entity: E): D

    protected fun getEntityId(id: Long): E =
        repository.findById(id).orElseThrow { IdNotFoundException(Example::class, id) }

    protected fun setBaseDTOFields(
        dto: D,
        entity: E,
    ) {
        dto.id = entity.id
        dto.createdDate = entity.createdDate?.let { transformInstantToLocalDateTime(it) }
        dto.modifiedDate = entity.modifiedDate?.let { transformInstantToLocalDateTime(it) }
    }

    private fun transformInstantToLocalDateTime(instant: Instant): LocalDateTime =
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
}
