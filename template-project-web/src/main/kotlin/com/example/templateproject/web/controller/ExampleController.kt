package com.example.templateproject.web.controller

import com.example.templateproject.api.dto.ErrorDTO
import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.core.service.ExampleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

const val EXAMPLE_ENDPOINT = "/example"

@RestController
@RequestMapping(EXAMPLE_ENDPOINT)
class ExampleController(private val exampleService: ExampleService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ExampleController::class.java)
    }

    @GetMapping(produces = [ "application/json" ])
    @Operation(summary = "Gets paginated and sorted Examples.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "The Example list has been successfully returned.",
            useReturnTypeSchema = true)
    )
    @PageableAsQueryParam
    fun getExamples(@Parameter(hidden = true) @PageableDefault pageable: Pageable): PageDetails<ExampleDTO> {
        return exampleService.getExamples(pageable)
            .apply { LOGGER.info("Returning ${content.size} out of $totalElements Examples") }
    }

    @GetMapping(path =  [ "search" ], produces = [ "application/json" ])
    @Operation(summary = "Gets paginated and sorted Examples based the search terms.")
    @ApiResponses(
        ApiResponse(responseCode = "200",
            description = "The Example list has been successfully returned based on the search terms.",
            useReturnTypeSchema = true)
    )
    @PageableAsQueryParam
    fun searchExamples(@Parameter(hidden = true) @PageableDefault pageable: Pageable,
                       @RequestParam searchTerms: List<String>
    ): PageDetails<ExampleDTO> {
        val sanitizedSearchTerms = searchTerms.map { removeNonAllowedCharacters(it) }.toList()

        return exampleService.searchExamples(sanitizedSearchTerms, pageable)
            .apply { LOGGER.info("Returning ${content.size} out of $totalElements Examples " +
                    "for the given search terms: $sanitizedSearchTerms") }
    }

    @GetMapping("/{id:[0-9]*}")
    @Operation(summary = "Gets Example by its id.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "The requested Example has been successfully returned.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ExampleDTO::class)))
                ))
            ]),
        ApiResponse(responseCode = "404", description = "The requested Example was not found.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ErrorDTO::class)))
                ))
            ])
    )
    fun getExample(@PathVariable id: Long): ExampleDTO {
        LOGGER.info("Returning Example with id: $id")
        return exampleService.getExample(id)
    }

    @PostMapping(consumes = [ "application/json" ])
    @Operation(summary = "Creates a new Example.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Example successfully created.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ExampleDTO::class)))
                ))
            ])
    )
    fun createExample(@RequestBody @Valid request: ExampleDTO): RedirectView {
        LOGGER.info("Creating Example: ${StringUtils.trimAllWhitespace(request.toString())}")

        val id = exampleService.createExample(request)
        return RedirectView("/example/$id")
    }

    @PutMapping
    @Operation(summary = "Update an existing Example.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Example successfully updated.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ExampleDTO::class)))
                ))
            ]),
        ApiResponse(responseCode = "404", description = "The requested Example was not found.",
            content = [
                (Content(mediaType = "application/json", array = (
                        ArraySchema(schema = Schema(implementation = ErrorDTO::class)))
                ))
            ])
    )
    fun updateExample(@RequestBody @Valid request: ExampleDTO): RedirectView {
        LOGGER.info("Updating Example: ${StringUtils.trimAllWhitespace(request.toString())}")

        val id = exampleService.updateExample(request)
        return RedirectView("/example/$id")
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes an Example by its id.")
    @ApiResponses(
        ApiResponse(responseCode = "204")
    )
    fun deleteExample(@PathVariable id: Long): ResponseEntity<Void> {
        LOGGER.info("Deleting Example with id: $id")

        exampleService.deleteExample(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/statistics", produces = [ "application/json" ])
    @Operation(summary = "Returns the number of words for each user in order of cardinality.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "The word count map.",
            useReturnTypeSchema = true)
    )
    fun getWordStatistics() = ResponseEntity.ok(exampleService.getWordCountForUsers())
}
