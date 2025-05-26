package com.example.templateproject.persistence.entity.mapper

import com.example.templateproject.persistence.entity.BaseEntity
import com.example.templateproject.persistence.entity.history.BaseHistoryEntity
import com.example.templateproject.persistence.entity.history.HistoryEvent

abstract class AbstractHistoryMapper<E : BaseEntity, H : BaseHistoryEntity> {

    abstract fun toHistoryEntity(entity: E, event: HistoryEvent): H
}
