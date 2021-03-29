package com.example.entity

import org.springframework.data.jpa.repository.JpaRepository

interface ExampleRepository: JpaRepository<Example, Long>