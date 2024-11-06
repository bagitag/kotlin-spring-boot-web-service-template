package com.example.templateproject.core.exception

import org.springframework.http.HttpStatus
import kotlin.reflect.KClass

data class IdNotFoundException(
    val clazz: KClass<*>,
    val entityId: Long
) : BaseException("Could not find ${clazz.simpleName} with id: $entityId", HttpStatus.NOT_FOUND)
