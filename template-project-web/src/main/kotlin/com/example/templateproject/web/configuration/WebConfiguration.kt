package com.example.templateproject.web.configuration

import com.example.templateproject.TemplateApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

private const val API_PATH_PREFIX = "/api"
private const val API_VERSION = "v1"
const val API_BASE_PATH = "$API_PATH_PREFIX/$API_VERSION"

@Configuration
class WebConfiguration : WebMvcConfigurer {

    val basePackageName: String = TemplateApplication::class.java.`package`.name

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.addPathPrefix(
            API_BASE_PATH,
            HandlerTypePredicate.forAnnotation(RestController::class.java)
                .and(HandlerTypePredicate.forBasePackage("$basePackageName.web.controller"))
        )
    }
}
