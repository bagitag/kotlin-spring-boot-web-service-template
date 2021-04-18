package com.example.service

import com.example.dto.ExampleDTO
import com.example.entity.Example
import com.example.exception.IdNotFoundException
import com.example.mapper.ExampleMapper
import com.example.repository.ExampleRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ExampleService(
    private val exampleRepository: ExampleRepository,
    private val exampleMapper: ExampleMapper
) {

    fun getAllExamples(): List<ExampleDTO> = exampleRepository.findAll().map { exampleMapper.toDTO(it) }

    fun getExample(id: Long) : Optional<ExampleDTO> = exampleRepository.findById(id).map { exampleMapper.toDTO(it) }

    fun createExample(dto: ExampleDTO): Long {
        val example = exampleMapper.fromDTO(dto)
        return exampleRepository.save(example).id!!
    }

    fun updateExample(dto: ExampleDTO): Long {
        return exampleRepository.findById(dto.id!!)
            .map { Example(it.id, dto.name) }
            .map { exampleRepository.save(it) }
            .map { it.id!! }
            .get()
    }

    fun deleteExample(id: Long) {
        exampleRepository.deleteById(id)
    }
}