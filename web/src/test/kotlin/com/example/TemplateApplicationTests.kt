package com.example

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.util.StreamUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.readBytes

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=test",
    "spring.datasource.password=12345",

    "spring.liquibase.enabled=false",

    "app.debug.logging.package.list=nothing"
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
            val ret = File.createTempFile("download", "tmp")
            StreamUtils.copy(it.body, FileOutputStream(ret))
            ret.readBytes()
        })

        assertTrue(compareContent(expected, actual), "The openapi.yaml file is outdated! $OPEN_API_ERROR_MSG")
    }

    private fun compareContent(expected: ByteArray, actual: ByteArray): Boolean {
        return replaceLineEndings(expected) == replaceLineEndings(actual)
    }

    private fun replaceLineEndings(array: ByteArray) =
        array.toString(Charset.defaultCharset()).replace("\r\n", "\n")
}
