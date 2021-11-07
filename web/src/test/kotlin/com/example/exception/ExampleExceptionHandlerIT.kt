package com.example.exception

import com.example.controller.ExampleController
import com.example.dto.ExampleDTO
import com.example.entity.Example
import com.example.metrics.ExceptionMetrics
import com.example.service.ExampleService
import io.micrometer.core.instrument.MockClock
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.hamcrest.CoreMatchers.startsWith
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ExampleController::class)
@ActiveProfiles("test")
@Import(MockClock::class, SimpleMeterRegistry::class, ExceptionMetrics::class)
class ExampleExceptionHandlerIT(@Autowired val mockMvc: MockMvc) {

    @MockBean
    private lateinit var exampleService: ExampleService

    @Test
    fun `Get example should return detailed 404 error if id does not exist`() {
        // given
        val id = 10L

        `when`(exampleService.getExample(id)).thenThrow(IdNotFoundException(Example::class, id))

        // when - then
        mockMvc.perform(MockMvcRequestBuilders.get("/example/$id").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.message").value("Could not find Example with id: $id"))
    }

    @Test
    fun `Update example should return detailed 404 error if id does not exist`() {
        // given
        val id = 10L
        val name = "New name"

        `when`(exampleService.updateExample(ExampleDTO(id, name))).thenThrow(IdNotFoundException(Example::class, id))

        // when - then
        mockMvc.perform(MockMvcRequestBuilders.put("/example?trace=true")
            .content("{ \"id\": $id, \"name\":\"$name\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").isNotEmpty)
            .andExpect(jsonPath("$.message").value("Could not find Example with id: $id"))
    }

    @Test
    fun `Get example should return detailed error with exception stack trace`() {
        // given
        val id = 10L

        `when`(exampleService.getExample(id)).thenThrow(RuntimeException())

        // when - then
        mockMvc.perform(MockMvcRequestBuilders.get("/example/$id")
            .param(ExampleExceptionHandler.stackTraceParameter, "true")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").isNotEmpty)
            .andExpect(jsonPath("$.message").value("Unknown internal server error"))
    }

    @Test
    fun `Create example should return detailed error if JSON is malformed`() {
        // given
        val id = 10L

        // when - then
        mockMvc.perform(MockMvcRequestBuilders.post("/example")
            .content("{ \"id\": $id }")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.message", startsWith("JSON parse error")))
    }
}
