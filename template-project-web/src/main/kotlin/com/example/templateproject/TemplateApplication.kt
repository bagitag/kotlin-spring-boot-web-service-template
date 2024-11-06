package com.example.templateproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
@PropertySource(value = [
    "classpath:persistence-\${spring.profiles.active:default}.properties",
    "classpath:client-\${spring.profiles.active:default}.properties",
    "classpath:core-\${spring.profiles.active:default}.properties"
], ignoreResourceNotFound = true)
class TemplateApplication

fun main(args: Array<String>) {
    runApplication<TemplateApplication>(*args)
}
