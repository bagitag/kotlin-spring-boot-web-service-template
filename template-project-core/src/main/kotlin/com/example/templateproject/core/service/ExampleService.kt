package com.example.templateproject.core.service

import com.example.templateproject.api.dto.ExampleDTO
import com.example.templateproject.api.dto.PageDetails
import com.example.templateproject.client.jsonplaceholder.JsonPlaceholderService
import com.example.templateproject.core.configuration.DatabaseCacheConfiguration
import com.example.templateproject.core.exception.IdNotFoundException
import com.example.templateproject.core.mapper.ExampleMapper
import com.example.templateproject.core.mapper.PageConverter
import com.example.templateproject.persistence.entity.Example
import com.example.templateproject.persistence.repository.ExampleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
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
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ExampleService::class.java)
    }

    @Cacheable(
        DatabaseCacheConfiguration.EXAMPLES_CACHE_NAME,
        condition = "#root.target.cacheEnabled"
    )
    fun getExamples(pageable: Pageable): PageDetails<ExampleDTO> {
        val pageableToUse = getPageable(pageable)
        return exampleRepository.findAll(pageableToUse)
            .map { exampleMapper.toDTO(it) }
            .let { pageConverter.createPageDetails(it) }
    }

    fun searchExamples(searchTerms: List<String>, pageable: Pageable): PageDetails<ExampleDTO> {
        val pageableToUse = getPageable(pageable)
        return exampleRepository.findByNameInIgnoreCase(searchTerms, pageableToUse)
            .map { exampleMapper.toDTO(it) }
            .let { pageConverter.createPageDetails(it) }
    }

    fun getExample(id: Long): ExampleDTO =
        exampleRepository.findById(id).map { exampleMapper.toDTO(it) }
            .orElseThrow { IdNotFoundException(Example::class, id) }

    @CacheEvict(
        value = [ DatabaseCacheConfiguration.EXAMPLES_CACHE_NAME ],
        beforeInvocation = false,
        allEntries = true
    )
    fun createExample(dto: ExampleDTO): Long {
        return exampleMapper.fromDTO(dto)
            .let { exampleRepository.save(it) }
            .id!!
    }

    @CacheEvict(
        value = [ DatabaseCacheConfiguration.EXAMPLES_CACHE_NAME ],
        beforeInvocation = false,
        allEntries = true
    )
    fun updateExample(dto: ExampleDTO): Long {
        return exampleRepository.findById(dto.id!!)
            .map { exampleMapper.fromDTO(dto) }
            .map { exampleRepository.save(it) }
            .map { it.id!! }
            .orElseThrow { IdNotFoundException(Example::class, dto.id!!) }
    }

    @CacheEvict(
        value = [ DatabaseCacheConfiguration.EXAMPLES_CACHE_NAME ],
        beforeInvocation = false,
        allEntries = true
    )
    fun deleteExample(id: Long) {
        exampleRepository.deleteById(id)
    }

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

    private fun getPageable(pageable: Pageable): Pageable {
        return if (pageable.sort.isUnsorted) {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, ExampleRepository.DEFAULT_SORT)
        } else {
            pageable
        }
    }

    fun test() {
        LOGGER.info("Test")
    }
}
