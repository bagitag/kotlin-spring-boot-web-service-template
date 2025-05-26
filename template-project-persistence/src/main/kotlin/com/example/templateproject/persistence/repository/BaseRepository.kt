package com.example.templateproject.persistence.repository

import com.example.templateproject.persistence.entity.BaseEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseRepository<E : BaseEntity> : JpaRepository<E, Long> {

    fun findByNameInIgnoreCase(searchTerms: List<String>, pageable: Pageable): Page<E>
}
