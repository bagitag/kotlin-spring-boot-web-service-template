package com.example.templateproject.persistence.repository.history

import com.example.templateproject.persistence.entity.history.BaseHistoryEntity
import com.example.templateproject.persistence.entity.history.HistoryEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseHistoryRepository<E : BaseHistoryEntity> : JpaRepository<E, Long> {

    fun findByEntityId(entityId: Long): List<E>

    fun findByEvent(event: HistoryEvent): List<E>

    fun findByEntityIdAndEvent(entityId: Long, event: HistoryEvent): List<E>
}
