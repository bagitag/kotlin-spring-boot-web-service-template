package com.example.templateproject.core.service

import com.example.templateproject.api.dto.BaseDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.core.exception.BadRequestErrorMessages
import com.example.templateproject.core.exception.BadRequestException
import com.example.templateproject.core.exception.IdNotFoundException
import com.example.templateproject.core.mapper.AbstractMapper
import com.example.templateproject.core.mapper.PageConverter
import com.example.templateproject.persistence.entity.BaseEntity
import com.example.templateproject.persistence.repository.BaseRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass

abstract class AbstractService<E : BaseEntity, D : BaseDTO>(
    protected open val repository: BaseRepository<E>,
    protected open val mapper: AbstractMapper<E, D>,
    protected open val pageConverter: PageConverter,
    protected open val clazz: KClass<E>,
) {
    companion object {
        val DEFAULT_SORT: Sort = Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("id"))
    }

    abstract fun validateEntity(entity: E)

    open fun getDefaultSort() = DEFAULT_SORT

    open fun getEntities(pageable: Pageable): PageDetails<D> {
        val pageableToUse = getPageable(pageable)
        return repository
            .findAll(pageableToUse)
            .map { mapper.toDTO(it) }
            .let { pageConverter.createPageDetails(it) }
    }

    open fun getEntityById(id: Long): D =
        repository
            .findById(id)
            .map { mapper.toDTO(it) }
            .orElseThrow { IdNotFoundException(clazz, id) }

    open fun createEntity(dto: D): D {
        if (dto.id != null) {
            throw BadRequestException(BadRequestErrorMessages.ID_MUST_BE_NULL)
        }

        return saveEntity(mapper.toEntity(dto))
            .let { mapper.toDTO(it) }
    }

    open fun updateEntity(dto: D): D {
        val id = dto.id

        if (id == null) {
            throw BadRequestException(BadRequestErrorMessages.ID_MUST_NOT_BE_NULL)
        }

        return repository
            .findById(dto.id!!)
            .map { mapper.toEntity(dto) }
            .map { saveEntity(it) }
            .map { mapper.toDTO(it) }
            .orElseThrow { IdNotFoundException(clazz, dto.id!!) }
    }

    open fun deleteEntity(id: Long) {
        repository.deleteById(id)
    }

    private fun saveEntity(entity: E): E {
        validateEntity(entity)

        return repository.save(entity)
    }

    protected fun getPageable(pageable: Pageable): Pageable =
        if (pageable.sort.isUnsorted) {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, getDefaultSort())
        } else {
            pageable
        }
}
