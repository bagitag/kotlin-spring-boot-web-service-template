package com.example.templateproject

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.util.StreamUtils
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readBytes

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=test",
    "spring.datasource.password=12345",

    "spring.liquibase.enabled=false",

    "client.jsonplaceholder.cache.enabled=false",
    "client.jsonplaceholder.cache.expiration.minutes=10",
    "client.jsonplaceholder.cache.users.maxSize=1",
    "client.jsonplaceholder.connection.timeout.millis=10000",
    "client.jsonplaceholder.read.timeout.millis=10000",

    "client.database.cache.enabled=false",
    "client.database.cache.expiration.minutes=10",

    "app.debug.logging.package.list=UNDEFINED"
])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TemplateApplicationTests(@Autowired val restTemplate: TestRestTemplate) {

    companion object {
        private const val OPEN_API_ERROR_MSG =  "Run the following command to update it: " +
                "mvnw clean verify -Dmaven.test.skip -Djacoco.skip=true -pl -jacoco-report -Popenapi"
    }

    @Test
    fun contextLoads() = Unit

    @Test
    @Throws(Exception::class)
    fun verifyOpenAPISpecification() {
        val file = Path.of("../openapi.yaml")
        val expected = try {
            file.readBytes()
        } catch (e: java.nio.file.NoSuchFileException) {
            System.err.println("\nThe openapi.yaml is missing! $OPEN_API_ERROR_MSG")
            throw e
        }
        val actual = restTemplate.execute("/api-docs.yaml", HttpMethod.GET, null, {
            val path = Files.createTempFile("download", ".tmp")
            StreamUtils.copy(it.body, FileOutputStream(path.toFile()))
            path.readBytes()
        })

        assertTrue(compareContent(expected, actual), "The openapi.yaml file is outdated! $OPEN_API_ERROR_MSG")
    }

    private fun compareContent(expected: ByteArray, actual: ByteArray): Boolean {
        return replaceLineEndings(expected) == replaceLineEndings(actual)
    }

    private fun replaceLineEndings(array: ByteArray) =
        array.toString(Charset.defaultCharset()).replace("\r\n", "\n")
}
