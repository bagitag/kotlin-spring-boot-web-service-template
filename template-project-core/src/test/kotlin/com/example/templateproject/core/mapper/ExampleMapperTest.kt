package com.example.templateproject.core.mapper

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.core.exception.IdNotFoundException
import com.example.templateproject.core.util.aMockExample
import com.example.templateproject.core.util.anExampleDTO
import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.persistence.repository.ExampleRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional
import kotlin.jvm.java

@ExtendWith(MockKExtension::class)
internal class ExampleMapperTest {
    @MockK
    private lateinit var repository: ExampleRepository

    @InjectMockKs
    private lateinit var victim: ExampleMapper

    @Test
    fun `Should create Entity from DTO without id`() {
        // given
        val name = "name"
        val dto = ExampleDTO(name)

        // when
        val actual = victim.toEntity(dto)

        // then
        assertEquals(name, actual.name)
        assertNull(actual.id)
        assertNull(actual.createdDate)
        assertNull(actual.modifiedDate)
    }

    @Test
    fun `Should throw exception when entity does not exist`() {
        // given
        val id = 100L
        val dto = anExampleDTO(id)

        every { repository.findById(id) } returns Optional.empty()

        // when - then
        assertThrows<IdNotFoundException> { victim.toEntity(dto) }
    }

    @Test
    fun `Should create Entity from DTO`() {
        // given
        val id = 100L
        val dto = anExampleDTO(id)

        val entity = mock(Example::class.java)
        `when`(entity.name).thenReturn(dto.name)
        `when`(entity.id).thenReturn(id)
        every { repository.findById(id) } returns Optional.of(entity)

        // when
        val actual = victim.toEntity(dto)

        // then
        assertEquals(dto.name, actual.name)
        assertEquals(id, actual.id)
        assertEquals(entity.createdDate, actual.createdDate)
        assertNull(actual.modifiedDate)
    }

    @Test
    fun `Should create DTO from Entity without base fields`() {
        // given
        val entity = Example("name")

        // when
        val actual = victim.toDTO(entity)

        // then
        assertEquals(entity.name, actual.name)
        assertNull(actual.id)
        assertNull(actual.createdDate)
        assertNull(actual.modifiedDate)
    }

    @Test
    fun `Should create DTO from Entity`() {
        // given
        val id = 100L
        val entity = aMockExample(id)

        // when
        val actual = victim.toDTO(entity)

        // then
        assertEquals(entity.name, actual.name)
        assertEquals(id, actual.id)
        assertEquals(LocalDateTime.ofInstant(entity.createdDate, ZoneId.systemDefault()), actual.createdDate)
        assertEquals(LocalDateTime.ofInstant(entity.modifiedDate, ZoneId.systemDefault()), actual.modifiedDate)
    }
}
