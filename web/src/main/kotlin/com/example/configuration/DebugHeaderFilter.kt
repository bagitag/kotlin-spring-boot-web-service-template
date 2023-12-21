package com.example.configuration

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.RandomStringUtils
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
        const val MDC_KEY = "debugLevel"
        const val MDC_VALUE = "on"
        const val REQUEST_ID = "requestId"
        private const val REQUEST_ID_LENGTH = 15
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest) {
            val debugHeader = request.getHeader(DEBUG_REQUEST_HEADER_NAME)

            if (!debugHeader.isNullOrEmpty() && debugHeader == DEBUG_REQUEST_HEADER_VALUE) {
                MDC.put(MDC_KEY, MDC_VALUE)
                val requestId = RandomStringUtils.randomAlphanumeric(REQUEST_ID_LENGTH)
                MDC.put(REQUEST_ID, "[$REQUEST_ID=$requestId]")
                LOGGER.debug("Debug level logging is turned on for $REQUEST_ID: {}", requestId)

                if (response is HttpServletResponse) {
                    response.addHeader(REQUEST_ID, requestId)
                }
            }

            chain.doFilter(request, response)

            MDC.remove(MDC_KEY)
        } else {
            chain.doFilter(request, response)
        }
    }
}
