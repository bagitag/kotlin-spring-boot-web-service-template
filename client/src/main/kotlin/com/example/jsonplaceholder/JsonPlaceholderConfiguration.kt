package com.example.jsonplaceholder

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.ClientHttpRequestFactories
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.time.Duration

@Configuration
@EnableRetry
class JsonPlaceholderConfiguration(
    @Value("\${client.jsonplaceholder.base-url}") private var baseUrl: String,
    @Value("\${client.jsonplaceholder.api-key}") private var apiKey: String,
    @Value("\${client.jsonplaceholder.connection.timeout.millis:1000}") private var connectionTimeoutMillis: Long,
    @Value("\${client.jsonplaceholder.read.timeout.millis:5000}") private var readTimeoutMillis: Long
) {

    companion object {
        private const val API_KEY_HEADER = "api-key"
    }

    @Bean
    fun jsonPlaceholderClient(): JsonPlaceholderClient {
        val restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(API_KEY_HEADER, apiKey)
            .requestFactory(clientHttpRequestFactory())
            .build()
        val factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build()
        return factory.createClient(JsonPlaceholderClient::class.java)
    }

    private fun clientHttpRequestFactory() = ClientHttpRequestFactories.get(
        ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofMillis(connectionTimeoutMillis))
            .withReadTimeout(Duration.ofMillis(readTimeoutMillis))
    )
}
