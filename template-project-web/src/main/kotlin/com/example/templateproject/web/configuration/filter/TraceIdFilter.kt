package com.example.templateproject.web.configuration.filter

import io.micrometer.tracing.Tracer
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["management.otlp.tracing.export.enabled"], havingValue = "true")
class TraceIdFilter(private val tracer: Tracer) : Filter {

    companion object {
        const val TRACE_ID_HEADER = "X-Trace-ID"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest
            && response is HttpServletResponse
            && isValidRequestPath(request.requestURI)
        ) {
            tracer.currentTraceContext().context()?.traceId()?.let {
                response.addHeader(TRACE_ID_HEADER, it)
            }
        }
        chain.doFilter(request, response)
    }
}
