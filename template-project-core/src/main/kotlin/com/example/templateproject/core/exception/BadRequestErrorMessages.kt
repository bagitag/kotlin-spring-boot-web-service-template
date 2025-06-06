package com.example.templateproject.core.exception

enum class BadRequestErrorMessages(
    val message: String,
) {
    ID_MUST_BE_NULL("ID must be null"),
    ID_MUST_NOT_BE_NULL("ID must not be null"),
    NAME_MUST_START_WITH_A_NUMBER("Name must start with a number"),
}
