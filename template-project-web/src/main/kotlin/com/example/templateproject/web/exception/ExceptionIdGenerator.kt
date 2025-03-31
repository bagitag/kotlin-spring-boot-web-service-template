package com.example.templateproject.web.exception

import com.example.templateproject.TemplateApplication
import com.example.templateproject.web.controller.AbstractController
import com.example.templateproject.web.exception.ExampleExceptionHandler.Companion.exceptionIdLength
import org.slf4j.LoggerFactory
import org.springframework.util.DigestUtils

object ExceptionIdGenerator {
    private val LOGGER = LoggerFactory.getLogger(AbstractController::class.java)

    private val basePackageName: String = TemplateApplication::class.java.`package`.name
    private val classesToExcludeFromIdGeneration = listOf<String>(
        "com.example.templateproject.web.configuration.DebugHeaderFilter"
    )

    fun generateExceptionId(exception: Exception): String {
        return try {
            val stackTraceElement = StackWalker.getInstance().walk { exception.stackTrace }.find { findClassName(it) }
                ?: StackWalker.getInstance().walk { exception.stackTrace }.first()

            val exceptionIdString = exception.javaClass.canonicalName +
                    stackTraceElement.className + stackTraceElement.methodName
            DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(exceptionIdLength)
        } catch (e: Exception) {
            LOGGER.error("Unexpected error while generating exceptionId: $e")
            "unknown"
        }
    }

    private fun findClassName(ste: StackTraceElement) = ste.className.contains(basePackageName)
            && !classesToExcludeFromIdGeneration.contains(ste.className)
}
