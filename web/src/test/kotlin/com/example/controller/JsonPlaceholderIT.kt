package com.example.controller

import com.example.BaseIntegrationTest
import com.example.jsonplaceholder.api.Post
import com.example.jsonplaceholder.api.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.maciejwalkowiak.wiremock.spring.InjectWireMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.util.stream.LongStream

class JsonPlaceholderIT(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val mockMvc: MockMvc,
    @Value("\${client.jsonplaceholder.base-url}") private var baseUrl: String,
    @Value("\${client.jsonplaceholder.api-key}") private var apiKey: String
): BaseIntegrationTest() {

    @InjectWireMock("json-placeholder")
    private lateinit var wiremock: WireMockServer

    @Test
    fun testForStatisticsEndpoint() {
        // given
        val users = listOf(
            User(1L, "name1", "username1", "email1"),
            User(2L, "name2", "username2", "email2"),
            User(3L, "name3", "username3", "email3")
        )

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

        // and
        createStubForPosts(1L, 2)
        createStubForPosts(2L, 1)
        createStubForPosts(3L, 0)

        // when
        val actual = mockMvc.perform(MockMvcRequestBuilders.get("/example/statistics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        // then
        assertNotNull(actual.response.contentAsString)
        val map = objectMapper.readValue(actual.response.contentAsString, HashMap::class.java)
        assertEquals(2, map!!.size)
        assertEquals(10, map["username1"])
        assertEquals(5, map["username2"])
        assertNull(map["username3"])

        wiremock.verify(1, getRequestedFor(urlEqualTo("/users")));
        wiremock.verify(3, getRequestedFor(urlPathEqualTo("/posts")));
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
}
