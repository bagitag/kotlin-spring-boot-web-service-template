package com.example.templateproject.web.configuration.filter

import com.example.templateproject.web.configuration.API_BASE_PATH

fun isValidRequestPath(requestURI: String) = requestURI.startsWith(API_BASE_PATH)
