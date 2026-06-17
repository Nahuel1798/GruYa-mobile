package com.example.gruya.ui.screens.quotes_list

import com.example.gruya.domain.model.Quote

data class QuotesListUiState(
    val quotes: List<Quote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionLoading: Int? = null
)
