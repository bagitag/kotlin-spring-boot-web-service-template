package com.example.exception

import com.example.controller.ExampleController
import com.example.dto.ExampleDTO
import com.example.entity.Example
import com.example.service.ExampleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ExampleController::class)
@ActiveProfiles("test")
class ExampleExceptionHandlerIT(@Autowired val mockMvc: MockMvc) {

    @MockBean
    private lateinit var exampleService: ExampleService

    @Test
    fun `Get example should return detailed 404 error if id does not exist`() {
        // given
        val id = 10L

        `when`(exampleService.getExample(id)).thenThrow(IdNotFoundException(Example::class, id))

        // when
        val action = mockMvc.perform(MockMvcRequestBuilders.get("/example/$id").contentType(MediaType.APPLICATION_JSON))

        // then
        action.andExpect(status().isNotFound)
        action.andExpect { result -> assertTrue(result.resolvedException is IdNotFoundException) }
        action.andExpect { result -> assertEquals("Could not find Example with id: $id", result.resolvedException!!.message) }
        action.andExpect { jsonPath("$.id").isNotEmpty }
        action.andExpect { jsonPath("$.stackTrace").isEmpty }
        action.andExpect { jsonPath("$.message").value("Could not find Example with id: $id") }
    }

    @Test
    fun `Update example should return detailed 404 error if id does not exist`() {
        // given
        val id = 10L
        val name = "New name"

        `when`(exampleService.updateExample(ExampleDTO(id, name))).thenThrow(IdNotFoundException(Example::class, id))

        // when
        val action = mockMvc.perform(MockMvcRequestBuilders.put("/example")
            .content("{ \"id\": $id, \"name\":\"$name\"}")
            .contentType(MediaType.APPLICATION_JSON))

        // then
        action.andExpect(status().isNotFound)
        action.andExpect { result -> assertTrue(result.resolvedException is IdNotFoundException) }
        action.andExpect { result -> assertEquals("Could not find Example with id: $id", result.resolvedException!!.message) }
        action.andExpect { jsonPath("$.id").isNotEmpty }
        action.andExpect { jsonPath("$.stackTrace").isEmpty }
        action.andExpect { jsonPath("$.message").value("Could not find Example with id: $id") }
    }

    @Test
    fun `Get example should return detailed error with exception stack trace`() {
        // given
        val id = 10L

        `when`(exampleService.getExample(id)).thenThrow(RuntimeException())

        // when
        val action = mockMvc.perform(MockMvcRequestBuilders.get("/example/$id")
            .param(ExampleExceptionHandler.stackTraceParameter, "true")
            .contentType(MediaType.APPLICATION_JSON)
        )

        // then
        action.andExpect(status().isInternalServerError)
        action.andExpect { result -> assertTrue(result.resolvedException is java.lang.RuntimeException) }
        action.andExpect { jsonPath("$.id").isNotEmpty }
        action.andExpect { jsonPath("$.stackTrace").isNotEmpty }
        action.andExpect { jsonPath("$.message").value("Unknown internal server error") }
    }
}
