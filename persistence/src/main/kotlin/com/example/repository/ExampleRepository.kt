package com.example.repository

import com.example.entity.Example
import org.springframework.data.jpa.repository.JpaRepository

interface ExampleRepository : JpaRepository<Example, Long>
