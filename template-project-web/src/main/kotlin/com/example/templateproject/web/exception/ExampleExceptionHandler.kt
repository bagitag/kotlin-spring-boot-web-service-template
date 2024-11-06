package com.example.templateproject.web.exception

import com.example.templateproject.TemplateApplication
import com.example.templateproject.api.dto.ErrorDTO
import com.example.templateproject.core.exception.BaseException
import com.example.templateproject.web.metrics.ExceptionMetrics
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.util.DigestUtils
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.concurrent.ExecutionException

@ControllerAdvice
class ExampleExceptionHandler(
    @Value("\${app.stack.trace.enabled:false}") val printStackTraceEnabled: Boolean,
    private val exceptionMetrics: ExceptionMetrics
) : ResponseEntityExceptionHandler() {

    val basePackageName: String = TemplateApplication::class.java.`package`.name

    companion object {
        const val unknownError = "Unknown internal server error"
        const val stackTraceParameter = "trace"
        const val exceptionIdLength = 15
    }

    public override fun handleExceptionInternal(
        ex: java.lang.Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val responseEntity = handleExceptions(ex, request)
        return ResponseEntity(responseEntity.body as Any, statusCode)
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleExceptions(originalException: Exception, request: WebRequest): ResponseEntity<ErrorDTO> {
        val exception = unwrapException(originalException)
        val message = getMessage(exception)
        val details = getDetails(exception)
        val exceptionId = generateExceptionId(exception)
        val logPrefix = generateLogPrefix(exception)

        exceptionMetrics.updateExceptionCounter(exceptionId, exception.javaClass.simpleName)
        logger.error("${logPrefix}ExceptionId: $exceptionId - ${exception.message}", exception)

        val stackTrace = getStackTrace(exception, request)
        val body = ErrorDTO(exceptionId, message, details, stackTrace)
        val httpStatus = getHttpStatus(exception)
        return ResponseEntity(body, httpStatus)
    }

    private fun unwrapException(originalException: Exception) =
        when (originalException) {
            is ExecutionException -> originalException.cause as Exception
            else -> originalException
        }

    private fun getMessage(exception: Exception): String {
        return when (exception) {
            is MethodArgumentNotValidException -> "Invalid request content."
            else -> exception.message ?: unknownError
        }
    }

    private fun getDetails(exception: Exception): Any? {
        return when (exception) {
            is MethodArgumentNotValidException -> {
                val errors: Map<String, List<String>> = exception.bindingResult.allErrors
                    .groupBy { (it as FieldError).field }
                    .mapValues { (_, groupedErrors) -> groupedErrors.map { it.defaultMessage!! } }
                errors
            }

            else -> null
        }
    }

    private fun generateExceptionId(exception: Exception): String {
        return try {
            val stackTraceElement =
                StackWalker.getInstance().walk { exception.stackTrace }.find { it.className.contains(basePackageName) }
                    ?: StackWalker.getInstance().walk { exception.stackTrace }.first()

            val exceptionIdString = exception.javaClass.canonicalName +
                    stackTraceElement.className + stackTraceElement.methodName
            DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(exceptionIdLength)
        } catch (e: Exception) {
            logger.error("Unexpected error while generating exceptionId: $e")
            "unknown"
        }
    }

    private fun generateLogPrefix(exception: Exception): String {
        return when (exception) {
            is ExternalServiceException -> "[${exception.serviceName}] - "
            else -> ""
        }
    }

    private fun getStackTrace(exception: Exception, request: WebRequest): String? {
        return if (printStackTraceEnabled && isStackTraceRequested(request)) exception.stackTraceToString() else null
    }

    private fun isStackTraceRequested(request: WebRequest): Boolean {
        return request.getParameterValues(stackTraceParameter)?.any { !it.isNullOrBlank() && it.toBoolean() } ?: false
    }

    private fun getHttpStatus(exception: Exception): HttpStatusCode {
        return when (exception) {
            is BaseException -> exception.httpStatus
            is MethodArgumentNotValidException -> exception.statusCode
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }
}
