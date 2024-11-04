package com.example.exception

import org.springframework.http.HttpStatus

abstract class BaseException(message: String, val httpStatus: HttpStatus): RuntimeException(message)
