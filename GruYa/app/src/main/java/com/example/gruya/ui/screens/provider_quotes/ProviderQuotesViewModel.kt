package com.example.gruya.ui.screens.provider_quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.QuoteRepository
import com.example.gruya.domain.model.QuoteStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderQuotesViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderQuotesUiState())
    val uiState: StateFlow<ProviderQuotesUiState> = _uiState.asStateFlow()

    init {
        loadQuotes()
    }

    fun loadQuotes(filter: ProviderQuoteFilter? = null) {
        val activeFilter = filter ?: _uiState.value.selectedFilter
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedFilter = activeFilter) }
            fetchQuotes(activeFilter)
        }
    }

    fun onFilterSelected(filter: ProviderQuoteFilter) {
        loadQuotes(filter)
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            fetchQuotes(_uiState.value.selectedFilter)
        }
    }

    fun onCancelQuote(quoteId: Int) {
        viewModelScope.launch {
            val result = quoteRepository.cancel(quoteId)
            result.fold(
                onSuccess = {
                    loadQuotes()
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(error = throwable.message)
                    }
                }
            )
        }
    }

    private suspend fun fetchQuotes(filter: ProviderQuoteFilter) {
        val apiStatuses = when (filter) {
            ProviderQuoteFilter.ACEPTADAS -> listOf(QuoteStatus.ACEPTADA)
            ProviderQuoteFilter.PENDIENTES -> listOf(QuoteStatus.PENDIENTE)
            ProviderQuoteFilter.FINALIZADAS -> listOf(
                QuoteStatus.RECHAZADA,
                QuoteStatus.CANCELADA,
                QuoteStatus.EXPIRADA
            )
        }

        val result = quoteRepository.getMine(apiStatuses)
        result.fold(
            onSuccess = { quotes ->
                _uiState.update {
                    it.copy(quotes = quotes, isLoading = false, isRefreshing = false)
                }
            },
            onFailure = { throwable ->
                _uiState.update {
                    it.copy(error = throwable.message, isLoading = false, isRefreshing = false)
                }
            }
        )
    }
}
