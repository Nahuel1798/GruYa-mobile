package com.example.gruya.ui.screens.assistances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistancesViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistancesUiState())
    val uiState: StateFlow<AssistancesUiState> = _uiState.asStateFlow()

    init {
        loadAssistances()
    }

    fun loadAssistances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = assistanceRepository.getUserAssistances()
            result.fold(
                onSuccess = { assistances ->
                    _uiState.update {
                        it.copy(assistances = assistances, isLoading = false)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(error = throwable.message, isLoading = false)
                    }
                }
            )
        }
    }

    fun refresh() {
        loadAssistances()
    }
}
