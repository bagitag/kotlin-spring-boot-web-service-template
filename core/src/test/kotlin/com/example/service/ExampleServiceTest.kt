package com.example.service

import com.example.dto.ExampleDTO
import com.example.entity.Example
import com.example.exception.IdNotFoundException
import com.example.mapper.ExampleMapper
import com.example.repository.ExampleRepository
import com.example.util.anExample
import com.example.util.anExampleDTOFromEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ExampleServiceTest {

    private val exampleRepository = mockk<ExampleRepository>()
    @MockK
    private lateinit var exampleMapper: ExampleMapper
    @InjectMockKs
    private lateinit var victim: ExampleService

    @Test
    fun `Should return all examples`() {
        // given
        val id1 = 100L
        val id2 = 200L
        val example1 = anExample(id1)
        val example2 = anExample(id2)
        val examples = listOf(example1, example2)

        every { exampleRepository.findAll() } returns examples
        every { exampleMapper.toDTO(example1) } returns anExampleDTOFromEntity(example1)
        every { exampleMapper.toDTO(example2) } returns anExampleDTOFromEntity(example2)

        // when
        val actual = victim.getAllExamples()

        // then
        verifySequence {
            exampleRepository.findAll()
            exampleMapper.toDTO(example1)
            exampleMapper.toDTO(example2)
        }
        assertEquals(examples.size, actual.size)
        assertEquals(id2, actual.last().id)
        assertEquals("#$id2 example", actual.last().name)
    }

    @Test
    fun `Should return example for the given id`() {
        // given
        val id = 8L
        val example = anExample(id)

        every { exampleRepository.findById(id) } returns Optional.of(example)
        every { exampleMapper.toDTO(example) } returns anExampleDTOFromEntity(example)

        // when
        val actual = victim.getExample(id)

        // then
        verifySequence {
            exampleRepository.findById(id)
            exampleMapper.toDTO(example)
        }
        assertEquals(id, actual.id)
        assertEquals("#$id example", actual.name)
    }

    @Test
    fun `Should return exception for the given id`() {
        // given
        val id = 100L
        every { exampleRepository.findById(id) } returns Optional.empty()

        // when
        assertThrows<IdNotFoundException> { victim.getExample(id) }

        // then
        verifySequence {
            exampleRepository.findById(id)
        }
    }

    @Test
    fun `Should create new example and return id`() {
        // given
        val id = 99L
        val name = "test example"
        val exampleDTO = ExampleDTO(id, name)
        val example = Example(id, name)

        every { exampleMapper.fromDTO(exampleDTO) } returns example
        every { exampleRepository.save(example) } returns example

        // when
        val actual = victim.createExample(exampleDTO)

        // then
        verifySequence {
            exampleMapper.fromDTO(exampleDTO)
            exampleRepository.save(example)
        }
        assertEquals(example.id, actual)
    }
}
