package com.example.service

import com.example.dto.ExampleDTO
import com.example.entity.Example
import com.example.exception.IdNotFoundException
import com.example.mapper.ExampleMapper
import com.example.repository.ExampleRepository
import org.springframework.stereotype.Service

@Service
class ExampleService(
    private val exampleRepository: ExampleRepository,
    private val exampleMapper: ExampleMapper
) {

    fun getAllExamples(): List<ExampleDTO> = exampleRepository.findAll().map { exampleMapper.toDTO(it) }

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
}
