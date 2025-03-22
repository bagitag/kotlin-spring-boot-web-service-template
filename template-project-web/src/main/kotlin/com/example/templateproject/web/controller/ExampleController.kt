package com.example.templateproject.web.controller

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.core.service.ExampleService
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val EXAMPLE_ENDPOINT = "/example"

@RestController
@RequestMapping(EXAMPLE_ENDPOINT)
@Tag(name = "Example", description = "Operations for Examples.")
class ExampleController(private val exampleService: ExampleService) :
    AbstractController<ExampleDTO>(exampleService, ExampleDTO::class) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ExampleController::class.java)
    }

    @GetMapping(path = ["search"], produces = ["application/json"])
    @Timed(extraTags = ["path", "search"])
    @Operation(summary = "Gets paginated and sorted Examples based the search terms.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "The Example list has been successfully returned based on the search terms.",
            useReturnTypeSchema = true
        )
    )
    @PageableAsQueryParam
    fun searchExamples(
        @Parameter(hidden = true) @PageableDefault pageable: Pageable,
        @RequestParam searchTerms: List<String>
    ): PageDetails<ExampleDTO> {
        val sanitizedSearchTerms = searchTerms.map { removeNonAllowedCharacters(it) }.toList()

        return exampleService.searchExamples(sanitizedSearchTerms, pageable)
            .apply {
                LOGGER.info(
                    "Returning ${content.size} out of $totalElements Examples " +
                            "for the given search terms: $sanitizedSearchTerms"
                )
            }
    }

    @GetMapping("/statistics", produces = ["application/json"])
    @Operation(summary = "Returns the number of words for each user in order of cardinality.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200", description = "The word count map.",
            useReturnTypeSchema = true
        )
    )
    fun getWordStatistics(): ResponseEntity<Map<String, Int>> = ResponseEntity.ok(exampleService.getWordCountForUsers())
}
