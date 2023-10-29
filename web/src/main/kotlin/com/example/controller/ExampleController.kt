package com.example.controller

import com.example.dto.ErrorDTO
import com.example.dto.ExampleDTO
import com.example.service.ExampleService
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/example")
class ExampleController(private val exampleService: ExampleService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ExampleController::class.java)
    }

    @GetMapping
    @Timed(extraTags = ["path", "list"])
    @Operation(summary = "Gets all examples.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "The list of examples has been successfully returned.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ExampleDTO::class)))
                ))
            ])
    )
    fun getAllExamples(): List<ExampleDTO> {
        return exampleService.getAllExamples().apply { LOGGER.info("Returning $size examples") }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets example by its id.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "The requested examples has been successfully returned.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ExampleDTO::class)))
                ))
            ]),
        ApiResponse(responseCode = "404", description = "The requested example was not found.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ErrorDTO::class)))
                ))
            ])
    )
    @Timed(extraTags = ["path", "read"])
    fun getExample(@PathVariable id: Long): ExampleDTO {
        LOGGER.info("Returning example with id: $id")
        return exampleService.getExample(id)
    }

    @PostMapping
    @Operation(summary = "Creates a new example.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Example successfully created.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ExampleDTO::class)))
                ))
            ])
    )
    @Timed(extraTags = ["path", "create"])
    fun createExample(@RequestBody request: ExampleDTO): RedirectView {
        LOGGER.info("Creating example: $request")

        val id = exampleService.createExample(request)
        return RedirectView("/example/$id")
    }

    @PutMapping
    @Operation(summary = "Update an existing example.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Example successfully updated.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ExampleDTO::class)))
                ))
            ]),
        ApiResponse(responseCode = "404", description = "The requested example was not found.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ErrorDTO::class)))
                ))
            ])
    )
    @Timed(extraTags = ["path", "update"])
    fun updateExample(@RequestBody request: ExampleDTO): RedirectView {
        LOGGER.info("Updating example: $request")

        val id = exampleService.updateExample(request)
        return RedirectView("/example/$id")
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes an example by its id.")
    @ApiResponses(
        ApiResponse(responseCode = "204")
    )
    @Timed(extraTags = ["path", "delete"])
    fun deleteExample(@PathVariable id: Long): ResponseEntity<Void> {
        LOGGER.info("Deleting example with id: $id")

        exampleService.deleteExample(id)
        return ResponseEntity.noContent().build()
    }
}
