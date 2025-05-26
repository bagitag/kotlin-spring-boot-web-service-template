package com.example.templateproject.persistence.entity

import com.example.templateproject.persistence.configuration.EntityChangeListener
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class, EntityChangeListener::class)
abstract class BaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    val id: Long? = null,
    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    var createdDate: Instant? = null,
    @LastModifiedDate
    @Column(name = "last_modified_date")
    var modifiedDate: Instant? = null
)
