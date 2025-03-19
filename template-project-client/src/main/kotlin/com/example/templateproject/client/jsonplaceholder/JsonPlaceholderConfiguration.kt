package com.example.templateproject.client.jsonplaceholder

import com.example.templateproject.client.CustomClientRequestObservationConvention
import io.micrometer.observation.ObservationRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.time.Duration

@Configuration
class JsonPlaceholderConfiguration(
    @Value("\${client.jsonplaceholder.id}") private val clientId: String,
    @Value("\${client.jsonplaceholder.base-url}") private val baseUrl: String,
    @Value("\${client.jsonplaceholder.api-key}") private val apiKey: String,
    @Value("\${client.jsonplaceholder.connection.timeout.millis:1000}") private val connectionTimeoutMillis: Long,
    @Value("\${client.jsonplaceholder.read.timeout.millis:5000}") private val readTimeoutMillis: Long
) {

    companion object {
        private const val API_KEY_HEADER = "api-key"
    }

    @Bean
    fun jsonPlaceholderClient(observationRegistry: ObservationRegistry): JsonPlaceholderClient {
        val restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(API_KEY_HEADER, apiKey)
            .requestFactory(clientHttpRequestFactory())
            .observationRegistry(observationRegistry)
            .observationConvention(observationConvention())
            .build()
        val factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build()
        return factory.createClient(JsonPlaceholderClient::class.java)
    }

    private fun clientHttpRequestFactory() = SimpleClientHttpRequestFactory().apply {
        setConnectTimeout(Duration.ofMillis(connectionTimeoutMillis))
        setReadTimeout(Duration.ofMillis(readTimeoutMillis))
    }

    private fun observationConvention() = CustomClientRequestObservationConvention(clientId)
}
