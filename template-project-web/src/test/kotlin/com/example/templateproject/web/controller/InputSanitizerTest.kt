package com.example.templateproject.web.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class InputSanitizerTest {
    companion object {
        @JvmStatic
        fun testData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(" 0, ", " 0 "),
                Arguments.of($$"O$_!%#<.>ß$¤÷×Đä▓O", "O.O"),
            )
    }

    @ParameterizedTest
    @CsvSource("Abc123,Abc123", "-Ab@22,Ab22", "\nS2.2,S2.2")
    fun `Should remove not allowed characters for simple inputs`(
        input: String,
        expected: String,
    ) {
        val actual = removeNonAllowedCharacters(input)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @MethodSource("testData")
    fun `Should remove not allowed characters`(
        input: String,
        expected: String,
    ) {
        val actual = removeNonAllowedCharacters(input)
        assertEquals(expected, actual)
    }
}
