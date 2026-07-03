package com.example.gruya.data.remote.dtos.response

data class PagedResponse<T>(
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)
