package com.example.templateproject

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.readBytes

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = [
        "spring.config.import=classpath:client-openapi.properties,classpath:core-openapi.properties," +
            "classpath:persistence-openapi.properties",
    ],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureTestRestTemplate
class TemplateApplicationTests(
    @param:Autowired val restTemplate: TestRestTemplate,
) {
    companion object {
        private const val OPEN_API_ERROR_MSG =
            "Run the following command to update it: " +
                "mvnw clean verify -Dmaven.test.skip -Djacoco.skip=true -pl !template-project-jacoco-report -Popenapi"
    }

    @Test
    fun contextLoads() = Unit

    @Test
    @Throws(Exception::class)
    fun verifyOpenAPISpecification() {
        val file = Path.of("../openapi.yaml")
        val expected =
            try {
                file.readBytes()
            } catch (e: java.nio.file.NoSuchFileException) {
                System.err.println("\nThe openapi.yaml is missing! $OPEN_API_ERROR_MSG")
                throw e
            }

        val actual = restTemplate.getForObject("/api-docs.yaml", ByteArray::class.java)

        assertNotNull(actual)
        assertTrue(compareContent(expected, actual), "The openapi.yaml file is outdated! $OPEN_API_ERROR_MSG")
    }

    private fun compareContent(
        expected: ByteArray,
        actual: ByteArray,
    ): Boolean = replaceLineEndings(expected) == replaceLineEndings(actual)

    private fun replaceLineEndings(array: ByteArray) = array.toString(Charset.defaultCharset()).replace("\r\n", "\n")
}
