package com.example.templateproject.web.configuration.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(name = ["management.otlp.tracing.export.enabled"], havingValue = "false")
@Order(0)
class RequestIdFilter : Filter {
    companion object {
        const val REQUEST_ID_HEADER = "X-Request-ID"
        const val REQUEST_ID_MDC_KEY = "requestId"
    }

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        if (request is HttpServletRequest && isValidRequestPath(request.requestURI)) {
            var requestId = request.getHeader(REQUEST_ID_HEADER)

            if (requestId.isNullOrEmpty()) {
                requestId = UUID.randomUUID().toString()
            }

            MDC.put(REQUEST_ID_MDC_KEY, requestId)

            if (response is HttpServletResponse) {
                response.addHeader(REQUEST_ID_HEADER, requestId)
            }

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
