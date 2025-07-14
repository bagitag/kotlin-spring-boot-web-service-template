package com.example.templateproject.web.exception

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.client.exception.ExternalServiceExceptionHandler
import com.example.templateproject.core.exception.IdNotFoundException
import com.example.templateproject.core.service.ExampleService
import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.web.configuration.API_BASE_PATH
import com.example.templateproject.web.controller.EXAMPLE_ENDPOINT
import com.example.templateproject.web.controller.ExampleController
import com.example.templateproject.web.exception.GlobalExceptionHandler.Companion.STACK_TRACE_QUERY_PARAMETER_NAME
import com.example.templateproject.web.metrics.ExceptionMetrics
import io.micrometer.core.instrument.MockClock
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ExampleController::class)
@ActiveProfiles("test")
@Import(MockClock::class, SimpleMeterRegistry::class, ExceptionMetrics::class, ExternalServiceExceptionHandler::class)
class GlobalExceptionHandlerIT(
    @param:Autowired val mockMvc: MockMvc,
) {
    private val path = "$API_BASE_PATH/$EXAMPLE_ENDPOINT"

    @MockitoBean
    private lateinit var exampleService: ExampleService

    @Test
    fun `Get example should return detailed 404 error if id does not exist`() {
        // given
        val id = 10L

        `when`(exampleService.getEntityById(id)).thenThrow(IdNotFoundException(Example::class, id))

        // when - then
        mockMvc
            .perform(MockMvcRequestBuilders.get("$path/$id").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.message").value("Could not find Example with id: $id"))
            .andExpect(jsonPath("$.details").doesNotExist())
    }

    @Test
    fun `Update example should return detailed 404 error if id does not exist`() {
        // given
        val id = 10L
        val name = "New name"
        val dto = ExampleDTO(name).apply { this.id = id }
        `when`(exampleService.updateEntity(dto)).thenThrow(IdNotFoundException(Example::class, id))

        // when - then
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .put("$path?$STACK_TRACE_QUERY_PARAMETER_NAME=true")
                    .content("{ \"id\": $id, \"name\":\"$name\"}")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").isNotEmpty)
            .andExpect(jsonPath("$.message").value("Could not find Example with id: $id"))
            .andExpect(jsonPath("$.details").doesNotExist())
    }

    @Test
    fun `Get example should return detailed error with exception stack trace`() {
        // given
        val id = 10L

        `when`(exampleService.getEntityById(id)).thenThrow(RuntimeException())

        // when - then
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("$path/$id")
                    .param(STACK_TRACE_QUERY_PARAMETER_NAME, "true")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").isNotEmpty)
            .andExpect(jsonPath("$.message").value("Unknown internal server error"))
            .andExpect(jsonPath("$.details").doesNotExist())
    }

    @Test
    fun `Create example should return detailed error if JSON is malformed`() {
        // given
        val id = 10L

        // when - then
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post(path)
                    .content("{ \"id\": $id }")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.message", startsWith("JSON parse error")))
            .andExpect(jsonPath("$.details").doesNotExist())
    }

    @Test
    fun `Create example should return validation errors if content is not valid`() {
        validatePutAndPost(
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(path)
                    .content("{ \"name\": \"  \" }")
                    .contentType(MediaType.APPLICATION_JSON),
            ),
        )
    }

    @Test
    fun `Update example should return validation errors if content is not valid`() {
        validatePutAndPost(
            mockMvc.perform(
                MockMvcRequestBuilders
                    .put(path)
                    .content("{ \"id\": 1, \"name\": \"  \" }")
                    .contentType(MediaType.APPLICATION_JSON),
            ),
        )
    }

    private fun validatePutAndPost(action: ResultActions) {
        action
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.message", equalTo("Invalid request content.")))
            .andExpect(jsonPath("$.details.name.length()", `is`(2)))
            .andExpect(
                jsonPath(
                    "$.details.name[*]",
                    containsInAnyOrder("must not be blank", "must be between 3 and 20 characters"),
                ),
            )
    }
}
