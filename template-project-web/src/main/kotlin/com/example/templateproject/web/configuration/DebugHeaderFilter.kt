package com.example.templateproject.web.configuration

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class DebugHeaderFilter : Filter {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DebugHeaderFilter::class.java)
        const val DEBUG_REQUEST_HEADER_NAME = "Jh7rLp2q9w4s8xv"
        const val DEBUG_REQUEST_HEADER_VALUE = "Gk3sFp7vRq9w2Lx"
        const val DEBUG_MODE_MDC_KEY = "debugLevel"
        const val DEBUG_MODE_MDC_VALUE = "on"
        private val DISABLED_REQUEST_PATHS = listOf("/actuator")
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
            if (request is HttpServletRequest && validateRequestPath(request.requestURI)) {
            val debugHeader = request.getHeader(DEBUG_REQUEST_HEADER_NAME)

            if (!debugHeader.isNullOrEmpty() && debugHeader == DEBUG_REQUEST_HEADER_VALUE) {
                MDC.put(DEBUG_MODE_MDC_KEY, DEBUG_MODE_MDC_VALUE)

                val requestId = MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY)
                LOGGER.debug("Debug level logging is turned on for requestId: {}", requestId)
            }

            try {
                chain.doFilter(request, response)
            } finally {
                MDC.remove(DEBUG_MODE_MDC_KEY)
            }
        } else {
            chain.doFilter(request, response)
        }
    }

    private fun validateRequestPath(requestURI: String) =
        DISABLED_REQUEST_PATHS.stream().anyMatch { !requestURI.contains(it) }
}
