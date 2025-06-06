package com.example.templateproject.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.SequenceGenerator
import org.hibernate.Hibernate

@Entity
@SequenceGenerator(
    name = "sequenceGenerator",
    sequenceName = "example_id_seq",
    allocationSize = 1,
)
data class Example(
    @Column(length = 20)
    var name: String,
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null ||
            Hibernate.getClass(this) != Hibernate.getClass(other)
        ) {
            return false
        }
        other as Example
        return id == other.id
    }

    override fun hashCode(): Int = (Hibernate.unproxy(this) as Example).id?.hashCode() ?: 0

    override fun toString(): String =
        "Example(id=$id, name='$name', createdDate=$createdDate, modifiedDate=$modifiedDate)"
}
