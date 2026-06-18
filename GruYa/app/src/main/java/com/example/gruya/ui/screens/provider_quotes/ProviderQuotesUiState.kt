package com.example.gruya.ui.screens.provider_quotes

import com.example.gruya.domain.model.Quote

data class ProviderQuotesUiState(
    val quotes: List<Quote> = emptyList(),
    val selectedFilter: ProviderQuoteFilter = ProviderQuoteFilter.ACEPTADAS,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
