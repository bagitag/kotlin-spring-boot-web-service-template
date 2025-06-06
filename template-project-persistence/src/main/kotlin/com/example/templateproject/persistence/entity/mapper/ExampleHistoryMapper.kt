package com.example.templateproject.persistence.entity.mapper

import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.persistence.entity.history.ExampleHistory
import com.example.templateproject.persistence.entity.history.HistoryEvent
import org.springframework.stereotype.Service

@Service
class ExampleHistoryMapper : AbstractHistoryMapper<Example, ExampleHistory>() {
    override fun toHistoryEntity(
        entity: Example,
        event: HistoryEvent,
    ): ExampleHistory =
        ExampleHistory(
            entity.id!!,
            event,
            entity.name,
        )
}
