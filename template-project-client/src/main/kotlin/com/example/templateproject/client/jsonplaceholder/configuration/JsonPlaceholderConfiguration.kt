package com.example.templateproject.client.jsonplaceholder.configuration

import com.example.templateproject.client.CustomClientRequestObservationConvention
import com.example.templateproject.client.MdcDecorator
import com.example.templateproject.client.RequestIdClientHttpRequestInterceptor
import com.example.templateproject.client.jsonplaceholder.JsonPlaceholderClient
import io.micrometer.observation.ObservationRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.support.CompositeTaskDecorator
import org.springframework.core.task.support.ContextPropagatingTaskDecorator
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.time.Duration
import java.util.concurrent.Executor

@Configuration
class JsonPlaceholderConfiguration(
    private val properties: JsonPlaceholderProperties,
    private val requestInterceptor: RequestIdClientHttpRequestInterceptor?,
) {
    companion object {
        private const val API_KEY_HEADER = "api-key"
        private const val THREAD_NAME_PREFIX = "JPH-exec-"
    }

    @Bean
    fun jsonPlaceholderClient(observationRegistry: ObservationRegistry): JsonPlaceholderClient {
        val restClient =
            RestClient
                .builder()
                .baseUrl(properties.baseUrl)
                .defaultHeader(API_KEY_HEADER, properties.apiKey)
                .requestFactory(clientHttpRequestFactory())
                .observationRegistry(observationRegistry)
                .observationConvention(observationConvention())

        requestInterceptor?.let { restClient.requestInterceptor(it) }

        val factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient.build())).build()
        return factory.createClient(JsonPlaceholderClient::class.java)
    }

    @Bean
    fun jsonPlaceHolderExecutor(): Executor {
        val threadPoolTaskExecutor =
            ThreadPoolTaskExecutor().apply {
                corePoolSize = properties.threadPool.corePoolSize
                maxPoolSize = properties.threadPool.maxPoolSize
                queueCapacity = properties.threadPool.queueCapacity
                threadNamePrefix = THREAD_NAME_PREFIX
                setTaskDecorator(
                    CompositeTaskDecorator(
                        listOf(
                            ContextPropagatingTaskDecorator(),
                            MdcDecorator(),
                        ),
                    ),
                )
            }
        return threadPoolTaskExecutor
    }

    private fun clientHttpRequestFactory() =
        SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofMillis(properties.connectionTimeoutMillis))
            setReadTimeout(Duration.ofMillis(properties.readTimeoutMillis))
        }

    private fun observationConvention() = CustomClientRequestObservationConvention(properties.clientId)
}
