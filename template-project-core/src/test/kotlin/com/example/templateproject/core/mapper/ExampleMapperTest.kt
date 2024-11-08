package com.example.templateproject.core.mapper

import com.example.templateproject.core.util.anExample
import com.example.templateproject.core.util.anExampleDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ExampleMapperTest {

    private val victim = ExampleMapper()

    @Test
    fun `Should create DTO from Entity`() {
        // given
        val id = 100L
        val example = anExample(id)

        // when
        val actual = victim.toDTO(example)

        // then
        assertEquals(id, actual.id)
        assertEquals("$id. example", actual.name)
    }

    @Test
    fun `Should create Entity from DTO`() {
        // given
        val id = 100L
        val exampleDTO = anExampleDTO(id)

        // when
        val actual = victim.fromDTO(exampleDTO)

        // then
        assertEquals(id, actual.id)
        assertEquals("$id. example", actual.name)
    }

    @Test
    fun `Test for test function`() {
        // when
        val actual = victim.test(1)

        // then
        assertEquals("#1 Test", actual)
    }
}
