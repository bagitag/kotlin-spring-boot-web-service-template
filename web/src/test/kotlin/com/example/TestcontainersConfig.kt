package com.example

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import javax.sql.DataSource

@TestConfiguration
@ConditionalOnProperty(value = ["testcontainers.enabled"], havingValue = "true", matchIfMissing = true)
@Testcontainers
class TestcontainersConfig {
    companion object {
        @ServiceConnection
        @Container
        private val databaseContainer: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>("postgres:latest")
            .apply { start() }
    }

    @Bean
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .url(databaseContainer.jdbcUrl)
            .username(databaseContainer.username)
            .password(databaseContainer.password)
            .build()
    }
}
