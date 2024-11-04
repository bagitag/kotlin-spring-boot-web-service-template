package com.example.templateproject.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import org.hibernate.Hibernate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Example(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exampleSequenceGenerator")
    @SequenceGenerator(name = "exampleSequenceGenerator", sequenceName = "example_id_seq", allocationSize = 1)
    val id: Long?,
    @Column(length = 20)
    val name: String,
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    var createdDate: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Example
        return id == other.id
    }

    override fun hashCode(): Int = (Hibernate.unproxy(this) as Example).id?.hashCode() ?: 0

    override fun toString(): String = "Example(id=$id, name='$name', createdDate=$createdDate)"
}
