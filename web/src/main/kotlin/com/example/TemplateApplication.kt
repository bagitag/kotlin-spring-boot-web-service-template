package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class TemplateApplication

fun main(args: Array<String>) {
	runApplication<TemplateApplication>(*args)
}
