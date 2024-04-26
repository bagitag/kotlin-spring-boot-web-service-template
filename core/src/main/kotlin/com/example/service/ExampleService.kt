package com.example.service

import com.example.dto.ExampleDTO
import com.example.dto.PageDetails
import com.example.entity.Example
import com.example.exception.IdNotFoundException
import com.example.mapper.ExampleMapper
import com.example.mapper.PageConverter
import com.example.repository.ExampleRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ExampleService(
    private val exampleRepository: ExampleRepository,
    private val exampleMapper: ExampleMapper,
    private val pageConverter: PageConverter
) {

    fun getExamples(pageable: Pageable): PageDetails<ExampleDTO> {
        val pageableToUse = getPageable(pageable)
        return exampleRepository.findAll(pageableToUse)
            .map { exampleMapper.toDTO(it) }
            .let { pageConverter.createPageDetails(it) }
    }

    fun searchExamples(searchTerms: List<String>, pageable: Pageable): PageDetails<ExampleDTO> {
        val pageableToUse = getPageable(pageable)
        return exampleRepository.findByNameInIgnoreCase(searchTerms, pageableToUse)
            .map { exampleMapper.toDTO(it) }
            .let { pageConverter.createPageDetails(it) }
    }

    fun getExample(id: Long): ExampleDTO =
        exampleRepository.findById(id).map { exampleMapper.toDTO(it) }
            .orElseThrow { IdNotFoundException(Example::class, id) }

    fun createExample(dto: ExampleDTO): Long {
        return exampleMapper.fromDTO(dto)
            .let { exampleRepository.save(it) }
            .id!!
    }

    fun updateExample(dto: ExampleDTO): Long {
        return exampleRepository.findById(dto.id!!)
            .map { exampleMapper.fromDTO(dto) }
            .map { exampleRepository.save(it) }
            .map { it.id!! }
            .orElseThrow { IdNotFoundException(Example::class, dto.id!!) }
    }

    fun deleteExample(id: Long) {
        exampleRepository.deleteById(id)
    }

    private fun getPageable(pageable: Pageable): Pageable {
        val pageableToUse = if (pageable.sort.isUnsorted) {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, ExampleRepository.DEFAULT_SORT)
        } else {
            pageable
        }
        return pageableToUse
    }
}
