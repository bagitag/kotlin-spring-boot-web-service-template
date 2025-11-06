package com.example.templateproject.client

import org.jboss.logging.MDC
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

@Configuration
@ConditionalOnBean(name = ["requestIdFilter"])
class RequestIdClientHttpRequestInterceptor : ClientHttpRequestInterceptor {
    companion object {
        const val REQUEST_ID_HEADER = "X-Request-ID"
        const val REQUEST_ID_MDC_KEY = "requestId"
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val requestId = MDC.get(REQUEST_ID_MDC_KEY)

        if (requestId != null) {
            request.headers.add(REQUEST_ID_HEADER, requestId.toString())
        }
        return execution.execute(request, body)
    }
}
