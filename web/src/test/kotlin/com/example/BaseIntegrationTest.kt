package com.example

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = [ "spring.profiles.active=test" ])
@ActiveProfiles("test")
class BaseIntegrationTest
