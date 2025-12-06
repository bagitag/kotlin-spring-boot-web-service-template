package com.example.templateproject.persistence.configuration

import com.example.templateproject.persistence.entity.BaseEntity
import com.example.templateproject.persistence.entity.history.BaseHistoryEntity
import com.example.templateproject.persistence.entity.history.HistoryEvent
import com.example.templateproject.persistence.entity.mapper.AbstractHistoryMapper
import com.example.templateproject.persistence.repository.history.BaseHistoryRepository
import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import jakarta.persistence.PreUpdate
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class EntityChangeListener(
    private val applicationContext: ApplicationContext,
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(EntityChangeListener::class.java)
        private const val HISTORY_MAPPER_SUFFIX = "HistoryMapper"
        private const val HISTORY_REPOSITORY_SUFFIX = "HistoryRepository"
    }

    @PrePersist
    fun postPersist(entity: BaseEntity) = saveHistoryEntity(entity, HistoryEvent.CREATE)

    @PreUpdate
    fun preUpdate(entity: BaseEntity) = saveHistoryEntity(entity, HistoryEvent.UPDATE)

    @PreRemove
    fun preRemove(entity: BaseEntity) = saveHistoryEntity(entity, HistoryEvent.DELETE)

    private fun saveHistoryEntity(
        entity: BaseEntity,
        event: HistoryEvent,
    ) {
        val mapper = getHistoryMapper(entity)
        val repository = getHistoryRepository(entity)

        if (mapper != null && repository != null) {
            val historyEntity = mapper.toHistoryEntity(entity, event)
            repository.save(historyEntity)
            LOGGER.debug("Created history entry for {} entity with id: {}", entity::class.simpleName, entity.id)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getHistoryMapper(entity: BaseEntity): AbstractHistoryMapper<BaseEntity, BaseHistoryEntity>? {
        val mapperBeanName = entity::class.simpleName + HISTORY_MAPPER_SUFFIX
        return applicationContext
            .getBeansOfType(AbstractHistoryMapper::class.java)
            .filterKeys { it.equals(mapperBeanName, ignoreCase = true) }
            .values
            .firstOrNull() as AbstractHistoryMapper<BaseEntity, BaseHistoryEntity>
    }

    @Suppress("UNCHECKED_CAST")
    private fun getHistoryRepository(entity: BaseEntity): BaseHistoryRepository<BaseHistoryEntity>? {
        val repositoryBeanName = entity::class.simpleName + HISTORY_REPOSITORY_SUFFIX
        return applicationContext
            .getBeansOfType(BaseHistoryRepository::class.java)
            .filterKeys { it.equals(repositoryBeanName, ignoreCase = true) }
            .values
            .firstOrNull() as BaseHistoryRepository<BaseHistoryEntity>
    }
}
