package com.example.service

import com.example.dto.ExampleDTO
import org.springframework.stereotype.Service

@Service
class ExampleService {

    companion object {
        var examples: MutableList<ExampleDTO> = MutableList(10) { ExampleDTO(it, "#$it example") }
    }

    fun getAllExamples(): List<ExampleDTO> = examples

    fun getExample(id: Int) : ExampleDTO = examples.find { it.id == id }!!

    fun createExample(dto: ExampleDTO): Int {
        return dto.apply { examples.add(this) }.id
    }
}