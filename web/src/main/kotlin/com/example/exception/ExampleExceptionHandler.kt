package com.example.exception

import com.example.dto.ErrorDTO
import com.example.metrics.ExceptionMetrics
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.DigestUtils
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExampleExceptionHandler(
    @Value("\${app.stack.trace.enabled:false}") val printStackTraceEnabled: Boolean,
    val exceptionMetrics: ExceptionMetrics
) : ResponseEntityExceptionHandler() {

    companion object {
        const val unknownError = "Unknown internal server error"
        const val stackTraceParameter = "trace"
        const val basePackageName = "com.example"
        const val exceptionIdLength = 10
    }

    public override fun handleExceptionInternal(ex: java.lang.Exception, body: Any?, headers: HttpHeaders,
                                                status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val responseEntity = handleExceptions(ex, request)
        return ResponseEntity(responseEntity.body as Any, status)
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleExceptions(exception: Exception, request: WebRequest): ResponseEntity<ErrorDTO> {
        val message = exception.message ?: unknownError
        val exceptionId = generateExceptionId(exception)

        exceptionMetrics.updateExceptionCounter(exceptionId)
        logger.error("ExceptionId: $exceptionId - $message", exception)

        val stackTrace = getStackTrace(exception, request)
        val body = ErrorDTO(exceptionId, message, stackTrace)
        val httpStatus = getHttpStatus(exception)
        return ResponseEntity(body, httpStatus)
    }

    private fun generateExceptionId(exception: Exception): String {
        return try {
            val stackTraceElement =
                StackWalker.getInstance().walk { exception.stackTrace }.find { it.className.contains(basePackageName) }
                    ?: StackWalker.getInstance().walk { exception.stackTrace }.first()

            val exceptionIdString = exception.javaClass.canonicalName +
                    stackTraceElement.className + stackTraceElement.methodName + stackTraceElement.lineNumber
            DigestUtils.md5DigestAsHex(exceptionIdString.toByteArray()).take(exceptionIdLength)
        } catch (e: Exception) {
            logger.error("Unexpected error while generating exceptionId: $e")
            "unknown"
        }
    }

    private fun getStackTrace(exception: Exception, request: WebRequest): String? {
        return if (printStackTraceEnabled && isStackTraceRequested(request)) exception.stackTraceToString() else null
    }

    private fun isStackTraceRequested(request: WebRequest): Boolean {
        return request.getParameterValues(stackTraceParameter)?.any { !it.isNullOrBlank() && it.toBoolean() } ?: false
    }

    private fun getHttpStatus(exception: Exception): HttpStatus {
        return if (exception is BaseException) exception.httpStatus else HttpStatus.INTERNAL_SERVER_ERROR
    }
}
