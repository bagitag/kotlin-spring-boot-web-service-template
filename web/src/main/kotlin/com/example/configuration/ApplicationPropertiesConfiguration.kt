package com.example.configuration

import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

@Component
@Profile("dev")
@PropertySource(value = [ "classpath:persistence-dev.properties" ])
class DevProfile

@Component
@Profile("test")
@PropertySource(value = [ "classpath:persistence-test.properties" ])
class TestProfile

@Component
@PropertySource(value = [ "classpath:persistence.properties" ])
class DefaultProfile
