package com.example.templateproject.client

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@Configuration(proxyBeanMethods = false)
class AsyncConfiguration
