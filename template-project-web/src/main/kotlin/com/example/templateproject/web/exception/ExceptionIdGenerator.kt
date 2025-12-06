package com.example.templateproject.web.exception

import com.example.templateproject.TemplateApplication
import com.example.templateproject.web.controller.AbstractController
import org.slf4j.LoggerFactory
import org.springframework.util.DigestUtils

object ExceptionIdGenerator {
    private val LOGGER = LoggerFactory.getLogger(AbstractController::class.java)

    const val EXCEPTION_ID_LENGTH = 15

    private val basePackageName: String = TemplateApplication::class.java.`package`.name
    private val classesToExcludeFromIdGeneration =
        listOf(
            "com.example.templateproject.web.configuration.filter.DebugHeaderFilter",
        )

    fun generateExceptionId(exception: Exception): String =
        try {
            val stackTraceElement =
                StackWalker.getInstance().walk { exception.stackTrace }.find { findClassName(it) }
                    ?: StackWalker.getInstance().walk { exception.stackTrace }.first()

            val exceptionIdString =
                exception.javaClass.canonicalName +
                    stackTraceElement.className + stackTraceElement.methodName
            DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(EXCEPTION_ID_LENGTH)
        } catch (e: Exception) {
            LOGGER.error("Unexpected error while generating exceptionId:", e)
            "unknown"
        }

    private fun findClassName(ste: StackTraceElement) =
        ste.className.contains(basePackageName) &&
            !classesToExcludeFromIdGeneration.contains(ste.className)
}
