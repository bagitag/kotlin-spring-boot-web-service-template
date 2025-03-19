package com.example.templateproject.persistence.entity.history

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseHistoryEntity(
    open val entityId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open val event: HistoryEvent,
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "historySequenceGenerator")
    val id: Long? = null,
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: Instant? = null,
)
