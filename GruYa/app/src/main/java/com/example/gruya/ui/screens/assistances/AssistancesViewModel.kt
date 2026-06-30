package com.example.gruya.ui.screens.assistances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.ui.navigation.NavigationEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistancesViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    private val navigationEventBus: NavigationEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistancesUiState())
    val uiState: StateFlow<AssistancesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadAssistances()
        observeNavigationEvents()
    }

    private fun observeNavigationEvents() {
        viewModelScope.launch {
            navigationEventBus.notificationEvents.collect {
                loadAssistances(isRefreshing = true)
            }
        }
    }

    fun loadAssistances(isRefreshing: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hasData = _uiState.value.assistances.isNotEmpty() || _uiState.value.activeAssistance != null
            
            _uiState.update { 
                it.copy(
                    isLoading = !isRefreshing && !hasData,
                    isRefreshing = isRefreshing,
                    error = null
                )
            }
            
            val assistancesResult = assistanceRepository.getUserAssistances()
            val activeResult = assistanceRepository.getAssistanceActive()

            _uiState.update { state ->
                var newState = state.copy(
                    isLoading = false,
                    isRefreshing = false
                )
                
                assistancesResult.onSuccess { list ->
                    newState = newState.copy(assistances = list)
                }.onFailure { t ->
                    newState = newState.copy(error = t.message)
                }

                activeResult.onSuccess { response ->
                    newState = newState.copy(activeAssistance = response?.toDomain())
                }
                
                newState
            }
        }
    }

    fun onRefresh() {
        loadAssistances(isRefreshing = true)
    }

    fun cancelActiveAssistance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = assistanceRepository.cancelAssistance()
            result.onSuccess {
                loadAssistances()
            }.onFailure { t ->
                _uiState.update { it.copy(isLoading = false, error = t.message) }
            }
        }
    }
}
