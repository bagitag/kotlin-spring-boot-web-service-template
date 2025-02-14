package com.example.templateproject.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class BaseDTO(
    var id: Long? = null,
    var createdDate: LocalDateTime? = null,
    var modifiedDate: LocalDateTime? = null
)
