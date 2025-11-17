package com.example.templateproject.web.controller

fun removeNonAllowedCharacters(input: String) = input.replace("[^a-zA-Z0-9. ]".toRegex(), "")
