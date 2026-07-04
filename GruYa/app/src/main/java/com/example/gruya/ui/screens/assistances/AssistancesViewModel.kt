package com.example.gruya.ui.screens.assistances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.ui.navigation.NavigationEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistancesViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    private val navigationEventBus: NavigationEventBus,
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
                loadAssistances(isRefreshing = false)
            }
        }
    }

    fun loadAssistances(isRefreshing: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    isRefreshing = isRefreshing,
                    error = null
                )
            }
            
            try {
                // Ejecutamos ambas peticiones en paralelo
                val assistancesDeferred = async { assistanceRepository.getUserAssistances() }
                val activeDeferred = async { assistanceRepository.getAssistanceActive() }

                val assistancesResult = assistancesDeferred.await()
                val activeResult = activeDeferred.await()

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

                    activeResult.onSuccess { active ->
                        newState = newState.copy(activeAssistance = active)
                    }.onFailure { _ ->
                        // No asignamos error si la solicitud activa falla
                    }
                    
                    newState
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isRefreshing = false, 
                        error = e.message ?: "Ocurrió un error inesperado"
                    )
                }
            }
        }
    }

    fun onRefresh() {
        loadAssistances(isRefreshing = true)
    }

    fun cancelActiveAssistance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true, error = null) }
            
            assistanceRepository.cancelAssistance()
                .onSuccess {
                    // Recargamos los datos para reflejar el cambio
                    loadAssistances()
                }
                .onFailure { t ->
                    _uiState.update { 
                        it.copy(
                            isPerformingAction = false, 
                            error = t.message ?: "Error al cancelar la solicitud"
                        ) 
                    }
                }
            
            _uiState.update { it.copy(isPerformingAction = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
