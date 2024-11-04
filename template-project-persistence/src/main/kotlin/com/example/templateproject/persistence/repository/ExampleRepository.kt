package com.example.templateproject.persistence.repository

import com.example.templateproject.persistence.entity.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface ExampleRepository : JpaRepository<Example, Long> {

    companion object {
        val DEFAULT_SORT: Sort = Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("id"))
    }

    fun findByNameInIgnoreCase(searchTerms: List<String>, pageable: Pageable): Page<Example>
}
