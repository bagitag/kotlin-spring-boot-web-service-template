package com.example.templateproject.web.configuration.filter

import io.micrometer.tracing.Tracer
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@ConditionalOnProperty(name = ["management.tracing.export.enabled"], havingValue = "true")
class TraceIdFilter(
    private val tracer: Tracer,
) : OncePerRequestFilter() {
    companion object {
        const val TRACE_ID_HEADER = "X-Trace-ID"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (isValidRequestPath(request.requestURI)) {
            tracer.currentTraceContext().context()?.let {
                response.setHeader(TRACE_ID_HEADER, it.traceId())
            }
        }
        chain.doFilter(request, response)
    }
}
