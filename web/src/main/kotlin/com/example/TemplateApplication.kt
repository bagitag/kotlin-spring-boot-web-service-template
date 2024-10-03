package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
@PropertySource(value = [
    "classpath:persistence-\${spring.profiles.active:default}.properties",
    "classpath:client-\${spring.profiles.active:default}.properties"
])
class TemplateApplication

fun main(args: Array<String>) {
    runApplication<TemplateApplication>(*args)
}
