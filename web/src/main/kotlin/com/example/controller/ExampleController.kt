package com.example.controller

import com.example.dto.ExampleDTO
import com.example.service.ExampleService
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
    fun getAllExamples(): List<ExampleDTO> {
        return exampleService.getAllExamples().apply { LOGGER.info("Returning $size examples") }
    }

    @GetMapping("/{id}")
    fun getExample(@PathVariable id: Long): ExampleDTO {
        LOGGER.info("Returning example with id: $id")
        return exampleService.getExample(id)
    }

    @PostMapping
    fun createExample(@RequestBody request: ExampleDTO): RedirectView {
        LOGGER.info("Creating example: $request")

        val id = exampleService.createExample(request)
        return RedirectView("/example/$id")
    }

    @PutMapping
    fun updateExample(@RequestBody request: ExampleDTO): RedirectView {
        LOGGER.info("Updating example: $request")

        val id = exampleService.updateExample(request)
        return RedirectView("/example/$id")
    }

    @DeleteMapping("/{id}")
    fun deleteExample(@PathVariable id: Long): ResponseEntity<Void> {
        LOGGER.info("Deleting example with id: $id")

        exampleService.deleteExample(id)
        return ResponseEntity.noContent().build()
    }
}
