package com.example.templateproject.web.configuration.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@ConditionalOnProperty(name = ["management.tracing.export.enabled"], havingValue = "false")
@Order(0)
class RequestIdFilter : OncePerRequestFilter() {
    companion object {
        const val REQUEST_ID_HEADER = "X-Request-ID"
        const val REQUEST_ID_MDC_KEY = "requestId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (isValidRequestPath(request.requestURI)) {
            var requestId = request.getHeader(REQUEST_ID_HEADER)

            if (requestId.isNullOrEmpty()) {
                requestId = UUID.randomUUID().toString()
            }

            MDC.put(REQUEST_ID_MDC_KEY, requestId)

            response.setHeader(REQUEST_ID_HEADER, requestId)

            try {
                chain.doFilter(request, response)
            } finally {
                MDC.remove(REQUEST_ID_MDC_KEY)
            }
        } else {
            chain.doFilter(request, response)
        }
    }
}
