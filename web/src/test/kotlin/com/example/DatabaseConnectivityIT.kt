package com.example

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate

class DatabaseConnectivityIT: BaseIntegrationTest() {

    companion object {
        private val SUPPORTED_DATABASES = listOf("PostgreSQL")
    }

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun verifyDatabaseVendor() {
        val actualDatabaseVersion = jdbcTemplate.queryForObject("SELECT version()", String::class.java)
        assertNotNull(actualDatabaseVersion)
        assertTrue(SUPPORTED_DATABASES.stream().filter { actualDatabaseVersion.contains(it) }.findFirst().isPresent)
    }
}
