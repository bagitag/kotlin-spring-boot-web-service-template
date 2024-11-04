package com.example.templateproject.web.controller

import com.example.templateproject.client.jsonplaceholder.api.Post
import com.example.templateproject.client.jsonplaceholder.api.User
import com.example.templateproject.web.BaseIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.maciejwalkowiak.wiremock.spring.InjectWireMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.util.stream.LongStream

class JsonPlaceholderIT(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val cacheManager: CacheManager,
    @Value("\${client.jsonplaceholder.base-url}") private val baseUrl: String,
    @Value("\${client.jsonplaceholder.api-key}") private val apiKey: String
) : BaseIntegrationTest() {

    @InjectWireMock("json-placeholder")
    private lateinit var wiremock: WireMockServer

    private lateinit var users: List<User>

    @BeforeEach
    fun initialize() {
        users = listOf(
            User(1L, "name1", "username1", "email1"),
            User(2L, "name2", "username2", "email2"),
            User(3L, "name3", "username3", "email3")
        )

        createStubForPosts(1L, 2)
        createStubForPosts(2L, 1)
        createStubForPosts(3L, 0)

        cacheManager.cacheNames.forEach {
            cacheManager.getCache(it)?.clear()
        }
    }

    @Test
    fun testForStatisticsEndpoint() {
        // given
        wiremock.givenThat(
            get("/users")
                .withHost(equalTo(URI(baseUrl).host))
                .withHeader("api-key", equalTo(apiKey))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(users))
                )
        )

        // when
        val actual = mockMvc.perform(MockMvcRequestBuilders.get("/example/statistics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        // then
        verifyResponse(actual)

        wiremock.verify(1, getRequestedFor(urlEqualTo("/users")));
        wiremock.verify(3, getRequestedFor(urlPathEqualTo("/posts")));
    }

    @Test
    fun testForStatisticsEndpointWithRetry() {
        // given
        wiremock.givenThat(
            get("/users")
                .inScenario("Retry test")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("Internal Server Error")
                .withHost(equalTo(URI(baseUrl).host))
                .withHeader("api-key", equalTo(apiKey))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")
                )
        )

        wiremock.givenThat(
            get("/users")
                .inScenario("Retry test")
                .whenScenarioStateIs("Internal Server Error")
                .withHost(equalTo(URI(baseUrl).host))
                .withHeader("api-key", equalTo(apiKey))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(users))
                )
        )

        // when
        val actual = mockMvc.perform(MockMvcRequestBuilders.get("/example/statistics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        // then
        verifyResponse(actual)

        wiremock.verify(2, getRequestedFor(urlEqualTo("/users")));
        wiremock.verify(3, getRequestedFor(urlPathEqualTo("/posts")));
    }


    @Test
    fun testForStatisticsEndpointWithCache() {
        // given
        wiremock.givenThat(
            get("/users")
                .withHost(equalTo(URI(baseUrl).host))
                .withHeader("api-key", equalTo(apiKey))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(users))
                )
        )

        // when
        val actual1 = mockMvc.perform(MockMvcRequestBuilders.get("/example/statistics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val actual2 = mockMvc.perform(MockMvcRequestBuilders.get("/example/statistics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        // then
        verifyResponse(actual1)
        verifyResponse(actual2)

        wiremock.verify(1, getRequestedFor(urlEqualTo("/users")));
        wiremock.verify(6, getRequestedFor(urlPathEqualTo("/posts")));
    }

    private fun createStubForPosts(userId: Long, postCount: Long) {
        val posts = LongStream.range(0, postCount).boxed()
            .map { Post(it, userId, "title$it", "a a a a a") }
            .toList()

        wiremock.stubFor(
            get(urlPathEqualTo("/posts"))
                .withQueryParam("userId", equalTo(userId.toString()))
                .willReturn(okJson(objectMapper.writeValueAsString(posts)))
        )
    }

    private fun verifyResponse(actual: MvcResult) {
        assertNotNull(actual.response.contentAsString)
        val map = objectMapper.readValue(actual.response.contentAsString, HashMap::class.java)
        assertEquals(2, map!!.size)
        assertEquals(10, map["username1"])
        assertEquals(5, map["username2"])
        assertNull(map["username3"])
    }
}
