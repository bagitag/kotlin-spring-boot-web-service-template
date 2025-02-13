package com.example.templateproject.web.controller

import com.example.templateproject.api.dto.BaseDTO
import com.example.templateproject.api.dto.ErrorDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.core.service.AbstractService
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import kotlin.reflect.KClass

abstract class AbstractController<D : BaseDTO>(
    private val service: AbstractService<*, D>,
    private val clazz: KClass<D>
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AbstractController::class.java)
    }

    @GetMapping(produces = ["application/json"])
    @Timed(extraTags = ["path", "list"])
    @Operation(summary = "Gets paginated and sorted entities.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200", description = "The entity list has been successfully returned.",
            useReturnTypeSchema = true
        )
    )
    @PageableAsQueryParam
    fun getEntities(@Parameter(hidden = true) @PageableDefault pageable: Pageable): PageDetails<D> {
        return service.getEntities(pageable)
            .apply { LOGGER.info("Returning ${content.size} out of $totalElements ${clazz.simpleName}") }
    }

    @GetMapping("/{id:[0-9]*}", produces = ["application/json"])
    @Operation(summary = "Gets entity by its id.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "The requested entity has been successfully returned."),
        ApiResponse(
            responseCode = "404", description = "The requested entity was not found.",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = ErrorDTO::class))
            ]
        )
    )
    @Timed(extraTags = ["path", "read"])
    fun getEntityById(@PathVariable id: Long): D {
        LOGGER.info("Returning ${clazz.simpleName} with id: $id")
        return service.getEntityById(id)
    }

    @PostMapping(consumes = ["application/json"], produces = ["application/json"])
    @Operation(summary = "Creates a new entity.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Entity successfully created."),
        ApiResponse(
            responseCode = "400", description = "The request body is invalid.",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = ErrorDTO::class))
            ]
        )
    )
    @Timed(extraTags = ["path", "create"])
    fun createEntity(@RequestBody @Valid request: D): ResponseEntity<D> {
        LOGGER.info("Creating ${clazz.simpleName}: ${StringUtils.trimAllWhitespace(request.toString())}")
        val createdEntity = service.createEntity(request)
        return ResponseEntity.status(HttpStatus.CREATED).body<D>(createdEntity)
    }

    @PutMapping(consumes = ["application/json"], produces = ["application/json"])
    @Operation(summary = "Update an existing entity.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Entity successfully updated."),
        ApiResponse(
            responseCode = "404", description = "The requested entity was not found.",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = ErrorDTO::class))
            ]
        ),
        ApiResponse(
            responseCode = "400", description = "The request body is invalid.",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = ErrorDTO::class))
            ]
        )
    )
    @Timed(extraTags = ["path", "update"])
    fun updateEntity(@RequestBody @Valid request: D): D {
        LOGGER.info("Updating ${clazz.simpleName}: ${StringUtils.trimAllWhitespace(request.toString())}")
        return service.updateEntity(request)
    }

    @DeleteMapping("/{id:[0-9]*}")
    @Operation(summary = "Deletes an entity by its id.")
    @ApiResponses(ApiResponse(responseCode = "204"))
    @Timed(extraTags = ["path", "delete"])
    fun deleteEntity(@PathVariable id: Long): ResponseEntity<Void> {
        LOGGER.info("Deleting ${clazz.simpleName} with id: $id")
        service.deleteEntity(id)
        return ResponseEntity.noContent().build()
    }
}
