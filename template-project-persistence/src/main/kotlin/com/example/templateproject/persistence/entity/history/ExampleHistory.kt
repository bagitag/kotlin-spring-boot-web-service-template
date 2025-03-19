package com.example.templateproject.persistence.entity.history

import jakarta.persistence.Entity
import jakarta.persistence.SequenceGenerator
import org.hibernate.Hibernate

@Entity
@SequenceGenerator(name = "historySequenceGenerator", sequenceName = "example_history_id_seq", allocationSize = 1)
data class ExampleHistory(
    override var entityId: Long,
    override var event: HistoryEvent,

    var name: String
) : BaseHistoryEntity(entityId, event) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as ExampleHistory
        return id == other.id
    }

    override fun hashCode(): Int = (Hibernate.unproxy(this) as ExampleHistory).id?.hashCode() ?: 0

    override fun toString(): String =
        "ExampleHistory(id=$id, entityId=$entityId, name='$name', event=$event, createdAt=$createdAt)"
}
