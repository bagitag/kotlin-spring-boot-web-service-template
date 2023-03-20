package com.example.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Example(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exampleSequenceGenerator")
    @SequenceGenerator(name = "exampleSequenceGenerator", sequenceName = "example_seq", allocationSize = 1)
    val id: Long?,
    val name: String,
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    var createdDate: Instant? = null
)
