package com.example.gruya.ui.screens.quotes_list

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
class QuotesListViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuotesListUiState())
    val uiState: StateFlow<QuotesListUiState> = _uiState.asStateFlow()

    fun loadQuotes(assistanceId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = quoteRepository.getByAssistance(assistanceId)
            result.fold(
                onSuccess = { quotes ->
                    _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(error = throwable.message, isLoading = false) }
                }
            )
        }
    }

    fun accept(quoteId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = quoteId, error = null) }
            val result = quoteRepository.acceptQuote(quoteId)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            quotes = state.quotes.map { quote ->
                                if (quote.id == quoteId) quote.copy(status = QuoteStatus.ACEPTADA)
                                else quote
                            },
                            actionLoading = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(error = throwable.message, actionLoading = null) }
                }
            )
        }
    }

    fun reject(quoteId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = quoteId, error = null) }
            val result = quoteRepository.rejectQuote(quoteId)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            quotes = state.quotes.map { quote ->
                                if (quote.id == quoteId) quote.copy(status = QuoteStatus.RECHAZADA)
                                else quote
                            },
                            actionLoading = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(error = throwable.message, actionLoading = null) }
                }
            )
        }
    }
}
