package com.example.templateproject.core.util

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

val DEFAULT_TIME_ZONE: ZoneOffset = ZoneOffset.UTC
val DEFAULT_CLOCK: Clock = Clock.fixed(Instant.now(), DEFAULT_TIME_ZONE)
