package com.example.controller

fun removeNonAllowedCharacters(input: String) = input.replace("[^a-zA-Z0-9.\\s]".toRegex(), "")
