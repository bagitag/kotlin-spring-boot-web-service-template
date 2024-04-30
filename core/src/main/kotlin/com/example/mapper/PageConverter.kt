package com.example.mapper

import com.example.dto.PageDetails
import com.example.dto.SortOrder
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class PageConverter {

    fun <T> createPageDetails(source: org.springframework.data.domain.Page<T>) = PageDetails<T>(
        content = source.content,
        pageNumber = source.pageable.pageNumber,
        pageSize = source.pageable.pageSize,
        totalElements = source.totalElements,
        totalPages = source.totalPages,
        sorted = source.sort.isSorted,
        sortOrders = source.sort.map { createSortOrder(it) }.toList()
    )

    private fun createSortOrder(order: Sort.Order) = SortOrder(order.property, order.direction.name)
}
