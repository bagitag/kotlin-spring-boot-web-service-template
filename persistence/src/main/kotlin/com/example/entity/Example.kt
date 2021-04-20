package com.example.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator

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
