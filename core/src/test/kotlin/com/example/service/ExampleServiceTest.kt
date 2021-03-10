package com.example.service

import com.example.dto.ExampleDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ExampleServiceTest {

    private lateinit var victim: ExampleService

    @BeforeEach
    fun setUp() {
        victim = ExampleService()
    }

    @Test
    fun `Should return all 10 examples`() {
        // when
        val actual = victim.getAllExamples()

        // then
        Assertions.assertEquals(ExampleService.examples.size, actual.size)
        Assertions.assertEquals(9, actual.last().id)
        Assertions.assertEquals("#9 example", actual.last().name)
    }

    @Test
    fun `Should return example for the given id`() {
        // given
        val id = 8

        // when
        val actual = victim.getExample(id)

        // then
        Assertions.assertEquals(id, actual.id)
        Assertions.assertEquals("#$id example", actual.name)
    }

    @Test
    fun `Should create new example and return id`() {
        // given
        val example = ExampleDTO(99, "test example")

        // when
        val actual = victim.createExample(example)

        // then
        Assertions.assertEquals(example.id, actual)
    }
}