package com.example.controller

import com.example.dto.ExampleDTO
import com.example.service.ExampleService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/example")
class ExampleController(private val exampleService: ExampleService) {

    private val LOGGER = LoggerFactory.getLogger(ExampleController::class.java)

    @GetMapping
    fun getAllExamples(): List<ExampleDTO> {
        val examples = exampleService.getAllExamples()

        LOGGER.info("Returning ${examples.size} examples")

        return examples
    }

    @GetMapping("/{id}")
    fun getExample(@PathVariable id: Int): ExampleDTO {
        LOGGER.info("Returning examples with id: $id")

        return exampleService.getExample(id)
    }

    @PostMapping
    fun createExample(@RequestBody request: ExampleDTO): RedirectView {
        LOGGER.info("Creating example: $request")

        val id = exampleService.createExample(request)
        return RedirectView("/example/$id", true)
    }
}