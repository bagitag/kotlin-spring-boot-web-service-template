package com.example.templateproject.core.service

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.client.jsonplaceholder.JsonPlaceholderService
import com.example.templateproject.core.exception.BadRequestErrorMessages
import com.example.templateproject.core.exception.BadRequestException
import com.example.templateproject.core.mapper.ExampleMapper
import com.example.templateproject.core.mapper.PageConverter
import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.persistence.repository.ExampleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Service
class ExampleService(
    @Value("\${client.database.cache.enabled}") val cacheEnabled: Boolean,
    private val exampleRepository: ExampleRepository,
    private val exampleMapper: ExampleMapper,
    private val pageConverter: PageConverter,
    private val jsonPlaceholderService: JsonPlaceholderService
) : AbstractService<Example, ExampleDTO>(exampleRepository, exampleMapper, pageConverter, Example::class) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ExampleService::class.java)
        const val EXAMPLES_CACHE_NAME = "database-examples"
    }

    override fun validateEntity(entity: Example) {
        entity.name.let {
            if (!it.matches("\\d.*".toRegex())) {
                throw BadRequestException(BadRequestErrorMessages.NAME_MUST_START_WITH_A_NUMBER)
            }
        }
    }

    fun searchExamples(searchTerms: List<String>, pageable: Pageable): PageDetails<ExampleDTO> {
        val pageableToUse = getPageable(pageable)
        return exampleRepository.findByNameInIgnoreCase(searchTerms, pageableToUse)
            .map { exampleMapper.toDTO(it) }
            .let { pageConverter.createPageDetails(it) }
    }

    @Cacheable(
        EXAMPLES_CACHE_NAME,
        condition = "#root.target.cacheEnabled"
    )
    override fun getEntities(pageable: Pageable) = super.getEntities(pageable)

    @CacheEvict(
        value = [ EXAMPLES_CACHE_NAME ],
        beforeInvocation = false,
        allEntries = true
    )
    override fun createEntity(dto: ExampleDTO) = super.createEntity(dto)

    @CacheEvict(
        value = [ EXAMPLES_CACHE_NAME ],
        beforeInvocation = false,
        allEntries = true
    )
    override fun updateEntity(dto: ExampleDTO) = super.updateEntity(dto)

    @CacheEvict(
        value = [ EXAMPLES_CACHE_NAME ],
        beforeInvocation = false,
        allEntries = true
    )
    override fun deleteEntity(id: Long) = super.deleteEntity(id)

    fun getWordCountForUsers(): Map<String, Int> {
        val userNameWordCountMap = ConcurrentHashMap<String, Int>()

        jsonPlaceholderService.getUsers().thenComposeAsync {
            val futures = mutableListOf<CompletableFuture<Void>>()

            it.forEach { user ->
                val future = jsonPlaceholderService.getPostsByUserId(user.id).thenAccept { posts ->
                    posts.forEach { post ->
                        val wordCount = post.body.split("\\s+".toRegex()).size
                        userNameWordCountMap.merge(user.username, wordCount, Integer::sum)
                    }
                }
                futures.add(future)
            }
            val futureArray = futures.toTypedArray()
            CompletableFuture.allOf(*futureArray)
        }.thenApply {
            LOGGER.info("Calculated word count for users: {}", userNameWordCountMap)
        }.get()

        return userNameWordCountMap.entries.sortedByDescending { it.value }.associateBy({ it.key }, { it.value })
    }
}
