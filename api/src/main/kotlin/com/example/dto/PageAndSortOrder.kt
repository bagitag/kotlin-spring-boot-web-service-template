package com.example.dto

data class PageDetails<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val sorted: Boolean,
    val sortOrders: List<SortOrder>
)

data class SortOrder(
    val property: String,
    val direction: String
)
